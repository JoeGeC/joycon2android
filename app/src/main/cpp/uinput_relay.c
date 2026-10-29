// uinput relay process: runs as the shell user (u:r:shell:s0), which can open /dev/uinput.
// Creates one device from a setup message on stdin, writes event batches to it, and reports
// rumble back on stdout. Protocol: docs/virtual-gamepad.md#uinput-relay

#include <errno.h>
#include <fcntl.h>
#include <linux/uinput.h>
#include <poll.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <sys/ioctl.h>
#include <time.h>
#include <unistd.h>

#define MAX_MESSAGE 4096
#define MAX_EFFECTS 16

enum { SETUP = 1, EVENTS = 2 };

struct effect {
    int uploaded;
    int playing;
    uint16_t strong;
    uint16_t weak;
    uint16_t length_ms;
    int64_t ends_at_ms;  // 0 while playing without a length
};

static struct effect effects[MAX_EFFECTS];
static uint16_t reported_strong, reported_weak;

static int64_t now_ms(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (int64_t)ts.tv_sec * 1000 + ts.tv_nsec / 1000000;
}

static int read_exact(int fd, void *buf, size_t count) {
    size_t total = 0;
    while (total < count) {
        ssize_t n = read(fd, (char *)buf + total, count - total);
        if (n <= 0) return -1;
        total += n;
    }
    return 0;
}

struct cursor {
    const uint8_t *at;
    const uint8_t *end;
};

static int take(struct cursor *c, void *out, size_t size) {
    if ((size_t)(c->end - c->at) < size) return -1;
    memcpy(out, c->at, size);
    c->at += size;
    return 0;
}

static int take_string(struct cursor *c, char *out, size_t capacity) {
    uint8_t length;
    if (take(c, &length, 1) != 0 || length >= capacity) return -1;
    memset(out, 0, capacity);
    return take(c, out, length);
}

// Setup: u16 bus, vendor, product, version; u8-prefixed name and phys; u16 key count, u16 codes;
// u16 axis count, per axis u16 code and s32 min, max, fuzz, flat, resolution; u8 rumble effects.
static int create_device(int fd, struct cursor *c) {
    struct uinput_setup setup;
    memset(&setup, 0, sizeof setup);
    char phys[64];
    uint16_t count, code;
    if (take(c, &setup.id.bustype, 2) || take(c, &setup.id.vendor, 2) || take(c, &setup.id.product, 2) ||
        take(c, &setup.id.version, 2) || take_string(c, setup.name, UINPUT_MAX_NAME_SIZE) ||
        take_string(c, phys, sizeof phys))
        return -1;

    if (take(c, &count, 2)) return -1;
    if (count) ioctl(fd, UI_SET_EVBIT, EV_KEY);
    for (uint16_t i = 0; i < count; i++) {
        if (take(c, &code, 2)) return -1;
        ioctl(fd, UI_SET_KEYBIT, code);
    }

    if (take(c, &count, 2)) return -1;
    if (count) ioctl(fd, UI_SET_EVBIT, EV_ABS);
    for (uint16_t i = 0; i < count; i++) {
        struct uinput_abs_setup axis;
        memset(&axis, 0, sizeof axis);
        if (take(c, &axis.code, 2) || take(c, &axis.absinfo.minimum, 4) || take(c, &axis.absinfo.maximum, 4) ||
            take(c, &axis.absinfo.fuzz, 4) || take(c, &axis.absinfo.flat, 4) || take(c, &axis.absinfo.resolution, 4))
            return -1;
        ioctl(fd, UI_SET_ABSBIT, axis.code);
        if (ioctl(fd, UI_ABS_SETUP, &axis) < 0) return -1;
    }

    uint8_t rumble_effects;
    if (take(c, &rumble_effects, 1)) return -1;
    if (rumble_effects) {
        ioctl(fd, UI_SET_EVBIT, EV_FF);
        ioctl(fd, UI_SET_FFBIT, FF_RUMBLE);
        setup.ff_effects_max = rumble_effects > MAX_EFFECTS ? MAX_EFFECTS : rumble_effects;
    }

    if (phys[0]) ioctl(fd, UI_SET_PHYS, phys);
    if (ioctl(fd, UI_DEV_SETUP, &setup) < 0) return -1;
    return ioctl(fd, UI_DEV_CREATE);
}

// Events: u16 count, then per event u16 type, u16 code, s32 value. A SYN_REPORT follows them.
static int write_events(int fd, struct cursor *c) {
    uint16_t count;
    if (take(c, &count, 2)) return -1;
    struct input_event ev;
    for (uint16_t i = 0; i <= count; i++) {
        memset(&ev, 0, sizeof ev);
        if (i < count) {
            if (take(c, &ev.type, 2) || take(c, &ev.code, 2) || take(c, &ev.value, 4)) return -1;
        } else {
            ev.type = EV_SYN;
            ev.code = SYN_REPORT;
        }
        if (write(fd, &ev, sizeof ev) < 0) return -1;
    }
    return 0;
}

// Overlapping effects play as the strongest of each motor.
static void report_rumble(void) {
    uint16_t strong = 0, weak = 0;
    for (int i = 0; i < MAX_EFFECTS; i++) {
        if (!effects[i].playing) continue;
        if (effects[i].strong > strong) strong = effects[i].strong;
        if (effects[i].weak > weak) weak = effects[i].weak;
    }
    if (strong == reported_strong && weak == reported_weak) return;
    reported_strong = strong;
    reported_weak = weak;
    printf("R %u %u\n", strong, weak);
    fflush(stdout);
}

static void erase(int fd, int request) {
    struct uinput_ff_erase er;
    memset(&er, 0, sizeof er);
    er.request_id = request;
    if (ioctl(fd, UI_BEGIN_FF_ERASE, &er) < 0) return;
    if (er.effect_id < MAX_EFFECTS) memset(&effects[er.effect_id], 0, sizeof(struct effect));
    er.retval = 0;
    ioctl(fd, UI_END_FF_ERASE, &er);
    report_rumble();
}

// A uinput device has no ff-memless to time effects, so each one's length is kept here.
static void play(int id, int value) {
    if (id >= MAX_EFFECTS || !effects[id].uploaded) return;
    effects[id].playing = value > 0;
    effects[id].ends_at_ms = value > 0 && effects[id].length_ms ? now_ms() + effects[id].length_ms : 0;
    report_rumble();
}

static void upload(int fd, int request) {
    struct uinput_ff_upload up;
    memset(&up, 0, sizeof up);
    up.request_id = request;
    if (ioctl(fd, UI_BEGIN_FF_UPLOAD, &up) < 0) return;
    int id = up.effect.id;
    if (up.effect.type == FF_RUMBLE && id >= 0 && id < MAX_EFFECTS) {
        effects[id].uploaded = 1;
        effects[id].strong = up.effect.u.rumble.strong_magnitude;
        effects[id].weak = up.effect.u.rumble.weak_magnitude;
        effects[id].length_ms = up.effect.replay.length;
        up.retval = 0;
    } else {
        up.retval = -EINVAL;
    }
    ioctl(fd, UI_END_FF_UPLOAD, &up);
    report_rumble();
}

static void read_device(int fd) {
    struct input_event ev;
    while (read(fd, &ev, sizeof ev) == sizeof ev) {
        if (ev.type == EV_UINPUT && ev.code == UI_FF_UPLOAD) upload(fd, ev.value);
        else if (ev.type == EV_UINPUT && ev.code == UI_FF_ERASE) erase(fd, ev.value);
        else if (ev.type == EV_FF) play(ev.code, ev.value);
    }
}

static void expire_effects(void) {
    int64_t now = now_ms();
    for (int i = 0; i < MAX_EFFECTS; i++) {
        if (effects[i].playing && effects[i].ends_at_ms && now >= effects[i].ends_at_ms) {
            effects[i].playing = 0;
            effects[i].ends_at_ms = 0;
        }
    }
    report_rumble();
}

static int poll_timeout_ms(void) {
    int64_t soonest = 0;
    for (int i = 0; i < MAX_EFFECTS; i++) {
        int64_t end = effects[i].ends_at_ms;
        if (effects[i].playing && end && (!soonest || end < soonest)) soonest = end;
    }
    if (!soonest) return -1;
    int64_t wait = soonest - now_ms();
    return wait < 0 ? 0 : (int)wait;
}

// Returns 1 to keep going, 0 on shutdown or a broken pipe.
static int read_message(int fd, int *created) {
    static uint8_t message[MAX_MESSAGE];
    uint32_t length;
    if (read_exact(STDIN_FILENO, &length, 4) != 0 || length == 0) return 0;
    if (length > MAX_MESSAGE || read_exact(STDIN_FILENO, message, length) != 0) {
        fprintf(stderr, "ERR bad message length %u\n", length);
        return 0;
    }
    struct cursor c = {message + 1, message + length};
    if (message[0] == SETUP && !*created) {
        if (create_device(fd, &c) != 0) {
            fprintf(stderr, "ERR create: %s\n", strerror(errno));
            return 0;
        }
        *created = 1;
        write(STDOUT_FILENO, "OK\n", 3);
    } else if (message[0] == EVENTS && *created) {
        if (write_events(fd, &c) != 0) fprintf(stderr, "ERR events: %s\n", strerror(errno));
    }
    return 1;
}

int main(void) {
    int fd = open("/dev/uinput", O_RDWR | O_NONBLOCK);
    if (fd < 0) {
        fprintf(stderr, "ERR open /dev/uinput: %s\n", strerror(errno));
        return 1;
    }

    int created = 0;
    struct pollfd fds[2] = {{STDIN_FILENO, POLLIN, 0}, {fd, POLLIN, 0}};
    for (;;) {
        if (poll(fds, 2, poll_timeout_ms()) < 0 && errno != EINTR) break;
        if (fds[1].revents & POLLIN) read_device(fd);
        expire_effects();
        if (fds[0].revents & (POLLIN | POLLHUP)) {
            if (!read_message(fd, &created)) break;
        }
    }

    if (created) ioctl(fd, UI_DEV_DESTROY);
    close(fd);
    return 0;
}
