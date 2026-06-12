/*
 * Debug DSU client for running on-device via adb: subscribes to a DSU server
 * and prints pad state. Companion to dsu_client.py for when the laptop has no
 * UDP route to the phone.
 *
 * Build: <ndk>/toolchains/llvm/prebuilt/<host>/bin/aarch64-linux-android24-clang -O2 -lz \
 *        -o dsu_client tools/dsu_client.c
 * Usage: dsu_client <host> [seconds]
 */
#include <arpa/inet.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <time.h>
#include <unistd.h>
#include <zlib.h>

#define PORT 26760

static double monotonic(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec + ts.tv_nsec / 1e9;
}

static void build_subscribe(uint8_t out[28]) {
    memset(out, 0, 28);
    memcpy(out, "DSUC", 4);
    out[4] = 1001 & 0xFF; out[5] = 1001 >> 8;
    out[6] = 12;                       /* payload length: type + flags/slot/mac */
    out[12] = 0x06; out[13] = 0xEB; out[14] = 0x0D; /* client id */
    out[16] = 0x02; out[18] = 0x10;    /* type 0x100002 */
    uint32_t crc = crc32(0, out, 28);
    memcpy(out + 8, &crc, 4);
}

static float read_float(const uint8_t *p) {
    float f;
    memcpy(&f, p, 4);
    return f;
}

int main(int argc, char **argv) {
    if (argc < 2) { fprintf(stderr, "usage: %s <host> [seconds]\n", argv[0]); return 1; }
    double duration = argc > 2 ? atof(argv[2]) : 60.0;

    int sock = socket(AF_INET, SOCK_DGRAM, 0);
    struct timeval timeout = {0, 500000};
    setsockopt(sock, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof timeout);

    struct sockaddr_in server = {0};
    server.sin_family = AF_INET;
    server.sin_port = htons(PORT);
    inet_pton(AF_INET, argv[1], &server.sin_addr);

    uint8_t subscribe[28];
    build_subscribe(subscribe);

    double deadline = monotonic() + duration;
    double next_subscribe = 0, next_motion = 0;
    uint8_t last_buttons[8] = {0xFF};
    long packets = 0;

    while (monotonic() < deadline) {
        double now = monotonic();
        if (now >= next_subscribe) {
            sendto(sock, subscribe, sizeof subscribe, 0, (struct sockaddr *)&server, sizeof server);
            next_subscribe = now + 1.0;
        }
        uint8_t data[256];
        ssize_t len = recv(sock, data, sizeof data, 0);
        if (len != 100 || memcmp(data, "DSUS", 4) != 0) continue;
        uint32_t type;
        memcpy(&type, data + 16, 4);
        if (type != 0x100002) continue;
        packets++;

        /* slot, bitmask1, bitmask2, home, touch, LS x/y, RS x/y */
        uint8_t buttons[8] = {data[20], data[36], data[37], data[38], data[39],
                              data[40], data[41], data[42]};
        if (memcmp(buttons, last_buttons, 8) != 0) {
            memcpy(last_buttons, buttons, 8);
            printf("[%8.2f] slot=%d b1=%02x b2=%02x home=%d touch=%d LS=(%d,%d) RS=(%d,%d) analog=",
                   now, data[20], data[36], data[37], data[38], data[39],
                   data[40], data[41], data[42], data[43]);
            for (int i = 44; i < 56; i++) printf("%02x", data[i]);
            printf("\n");
        }
        if (now >= next_motion) {
            next_motion = now + 0.25;
            printf("[%8.2f] accel=(%+6.2f,%+6.2f,%+6.2f)g gyro(pitch,yaw,roll)=(%+8.1f,%+8.1f,%+8.1f)dps\n",
                   now, read_float(data + 76), read_float(data + 80), read_float(data + 84),
                   read_float(data + 88), read_float(data + 92), read_float(data + 96));
        }
        fflush(stdout);
    }
    printf("done: %ld pad packets received\n", packets);
    return 0;
}
