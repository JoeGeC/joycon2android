// UHID relay process — runs under Shizuku shell context (u:r:shell:s0).
// Opens /dev/uhid and relays length-prefixed UHID events from stdin.
//
// Protocol (little-endian):
//   [4-byte length N] [N bytes of partial uhid_event]  (padded to full struct on write)
//   Length 0 = graceful shutdown.

#include <linux/uhid.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <errno.h>

static int read_exact(int fd, void *buf, size_t count) {
    size_t total = 0;
    while (total < count) {
        ssize_t n = read(fd, (char *)buf + total, count - total);
        if (n <= 0) return -1;
        total += n;
    }
    return 0;
}

int main(void) {
    int uhid_fd = open("/dev/uhid", O_RDWR);
    if (uhid_fd < 0) {
        fprintf(stderr, "ERR open /dev/uhid: %s\n", strerror(errno));
        return 1;
    }

    // Signal readiness
    write(STDOUT_FILENO, "OK\n", 3);

    // Event relay loop: read length-prefixed events from stdin, write to uhid
    for (;;) {
        uint32_t len;
        if (read_exact(STDIN_FILENO, &len, 4) != 0) break;
        if (len == 0) break;
        if (len > sizeof(struct uhid_event)) {
            fprintf(stderr, "ERR bad len %u\n", len);
            break;
        }

        struct uhid_event ev;
        memset(&ev, 0, sizeof(ev));
        if (read_exact(STDIN_FILENO, &ev, len) != 0) break;

        ssize_t ret = write(uhid_fd, &ev, sizeof(ev));
        if (ret < 0) {
            fprintf(stderr, "ERR write type=%u: %s\n", ev.type, strerror(errno));
            if (ev.type == UHID_CREATE2) break;
        }
    }

    // Cleanup
    struct uhid_event destroy;
    memset(&destroy, 0, sizeof(destroy));
    destroy.type = UHID_DESTROY;
    write(uhid_fd, &destroy, sizeof(destroy));
    close(uhid_fd);
    return 0;
}
