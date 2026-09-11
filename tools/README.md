# Debug tools

## DSU debug client

`dsu_client.c` subscribes to the app's DSU server and prints live pad state (buttons,
sticks, accel in g, gyro in deg/s). Used to verify wire content and to calibrate IMU
axes against known physical motions.

The server binds loopback, so compile with the NDK and run on-device against localhost
via adb:

  ```sh
  $ANDROID_NDK/toolchains/llvm/prebuilt/*/bin/aarch64-linux-android24-clang \
      -O2 -o /tmp/dsu_client tools/dsu_client.c -lz
  adb push /tmp/dsu_client /data/local/tmp/dsu_client
  adb shell /data/local/tmp/dsu_client 127.0.0.1 60
  ```

The third argument sets the motion print interval; it defaults to a readable 0.25 s, and
`0` prints every packet (~90 Hz), which is what differentiating the gravity vector needs.


### Axis calibration workflow

1. Capture while performing slow single-axis motions with holds (still → yaw left →
   pitch up → roll right), or any rich motion if direction labels aren't trusted.
2. Static holds anchor the accel frame (cemuhook: x=left, y=down, z=forward; flat at
   rest reads (0,−1,0)).
3. Gyro signs follow from the physics constraint `dv/dt = v × ω` applied to the
   normalized accel vector — fit the 16 sign combinations and break the mirror
   degeneracy with one static-hold anchor. (Done for the right Joy-Con, 2026-06;
   see `MotionConverter`.)

### Checking a gyro sign against gravity

Cheaper than the full fit, and it catches what the pointer cannot: roll turns about the
pointing axis, so a mirrored roll leaves the IMUIR cursor exactly where it is.

Capture *still → one deliberate single-axis turn → still*. Gravity is world-fixed, so
between the two holds its direction in the body frame turns with the controller: for unit
gravity `a0` before and `a1` after, the controller turned about `-(a0 × a1)` by the angle
between them. That axis must point the same way as the gyro integrated over the same
span, `∫ω dt`. Antiparallel means that axis' sign is wrong.

Only pitch and roll are visible this way — yaw turns about gravity itself, which leaves
the vector unchanged, so verify yaw against the pointer's horizontal response instead.
