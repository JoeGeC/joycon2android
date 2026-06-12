#!/usr/bin/env python3
"""Debug DSU client: subscribes to a DSU server and prints pad state.

Usage: python3 dsu_client.py <host> [seconds]

Prints a line whenever buttons change, and motion (accel g / gyro deg/s)
a few times per second. Used to verify wire content and calibrate IMU axes
against known physical motions.
"""

import socket
import struct
import sys
import time
import zlib

PORT = 26760
SUBSCRIBE_INTERVAL = 1.0
MOTION_PRINT_INTERVAL = 0.25

BUTTONS_1 = {0x80: "DLeft", 0x40: "DDown", 0x20: "DRight", 0x10: "DUp",
             0x08: "Options(+)", 0x04: "R3", 0x02: "L3", 0x01: "Share(-)"}
BUTTONS_2 = {0x80: "Square(Y)", 0x40: "Cross(B)", 0x20: "Circle(A)", 0x10: "Triangle(X)",
             0x08: "R1", 0x04: "L1", 0x02: "R2(ZR)", 0x01: "L2(ZL)"}


def client_packet(msg_type: int, payload: bytes) -> bytes:
    packet = bytearray(struct.pack("<4sHHII", b"DSUC", 1001, 4 + len(payload), 0, 0xDEB06))
    packet += struct.pack("<I", msg_type)
    packet += payload
    struct.pack_into("<I", packet, 8, zlib.crc32(bytes(packet)))
    return bytes(packet)


def names(mask: int, table: dict) -> str:
    return "+".join(name for bit, name in table.items() if mask & bit) or "none"


def main() -> None:
    host = sys.argv[1]
    duration = float(sys.argv[2]) if len(sys.argv) > 2 else 60.0
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(0.5)

    subscribe = client_packet(0x100002, bytes(8))  # flags=0: all slots
    deadline = time.monotonic() + duration
    next_subscribe = 0.0
    next_motion = 0.0
    last_buttons = None
    packets = 0

    while time.monotonic() < deadline:
        now = time.monotonic()
        if now >= next_subscribe:
            sock.sendto(subscribe, (host, PORT))
            next_subscribe = now + SUBSCRIBE_INTERVAL
        try:
            data, _ = sock.recvfrom(256)
        except socket.timeout:
            continue
        if len(data) != 100 or data[:4] != b"DSUS":
            print(f"[{now:8.2f}] unexpected packet: {len(data)} bytes")
            continue
        (msg_type,) = struct.unpack_from("<I", data, 16)
        if msg_type != 0x100002:
            continue
        packets += 1

        slot, b1, b2, home, touch = data[20], data[36], data[37], data[38], data[39]
        lx, ly, rx, ry = data[40], data[41], data[42], data[43]
        analog = data[44:56].hex()
        ax, ay, az, gp, gy, gr = struct.unpack_from("<6f", data, 76)

        buttons = (b1, b2, home, touch, lx, ly, rx, ry)
        if buttons != last_buttons:
            last_buttons = buttons
            print(f"[{now:8.2f}] slot={slot} b1={b1:02x}({names(b1, BUTTONS_1)}) "
                  f"b2={b2:02x}({names(b2, BUTTONS_2)}) home={home} touch={touch} "
                  f"LS=({lx},{ly}) RS=({rx},{ry}) analog={analog}")
        if now >= next_motion:
            next_motion = now + MOTION_PRINT_INTERVAL
            print(f"[{now:8.2f}] accel=({ax:+6.2f},{ay:+6.2f},{az:+6.2f})g "
                  f"gyro(pitch,yaw,roll)=({gp:+8.1f},{gy:+8.1f},{gr:+8.1f})dps")

    print(f"done: {packets} pad packets received")


if __name__ == "__main__":
    main()
