# Debug tools

## DSU debug clients

Two equivalent clients that subscribe to the app's DSU server and print live pad
state (buttons, sticks, accel in g, gyro in deg/s). Used to verify wire content and to
calibrate IMU axes against known physical motions.

- `dsu_client.py` — run from a machine with a UDP route to the phone (LAN mode on):

  ```sh
  python3 tools/dsu_client.py <phone-ip> [seconds]
  ```

- `dsu_client.c` — for when the laptop has no UDP route to the phone (AP isolation,
  macOS local-network privacy): compile with the NDK and run on-device against
  localhost via adb:

  ```sh
  $ANDROID_NDK/toolchains/llvm/prebuilt/*/bin/aarch64-linux-android24-clang \
      -O2 -o /tmp/dsu_client tools/dsu_client.c -lz
  adb push /tmp/dsu_client /data/local/tmp/dsu_client
  adb shell /data/local/tmp/dsu_client 127.0.0.1 60
  ```

### Axis calibration workflow

1. Capture while performing slow single-axis motions with holds (still → yaw left →
   pitch up → roll right), or any rich motion if direction labels aren't trusted.
2. Static holds anchor the accel frame (cemuhook: x=left, y=down, z=forward; flat at
   rest reads (0,−1,0)).
3. Gyro signs follow from the physics constraint `dv/dt = v × ω` applied to the
   normalized accel vector — fit the 16 sign combinations and break the mirror
   degeneracy with one static-hold anchor. (Done for the right Joy-Con, 2026-06;
   see `MotionConverter`.)
