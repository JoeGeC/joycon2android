#include <jni.h>
#include <linux/uhid.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>

#define TAG "UhidGamepad"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define REPORT_SIZE 13

// HID Report Descriptor: standard gamepad
// 14 buttons + hat switch + 2x 16-bit sticks + 2x 8-bit triggers
static const uint8_t rdesc[] = {
    0x05, 0x01,        // Usage Page (Generic Desktop)
    0x09, 0x05,        // Usage (Game Pad)
    0xA1, 0x01,        // Collection (Application)

    // Buttons (14 buttons, 2 bits padding)
    0x05, 0x09,        //   Usage Page (Button)
    0x19, 0x01,        //   Usage Minimum (Button 1)
    0x29, 0x0E,        //   Usage Maximum (Button 14)
    0x15, 0x00,        //   Logical Minimum (0)
    0x25, 0x01,        //   Logical Maximum (1)
    0x75, 0x01,        //   Report Size (1)
    0x95, 0x0E,        //   Report Count (14)
    0x81, 0x02,        //   Input (Data, Var, Abs)
    0x75, 0x01,        //   Report Size (1)
    0x95, 0x02,        //   Report Count (2) - padding
    0x81, 0x03,        //   Input (Const, Var, Abs)

    // Hat Switch (D-pad)
    0x05, 0x01,        //   Usage Page (Generic Desktop)
    0x09, 0x39,        //   Usage (Hat switch)
    0x15, 0x00,        //   Logical Minimum (0)
    0x25, 0x07,        //   Logical Maximum (7)
    0x35, 0x00,        //   Physical Minimum (0)
    0x46, 0x3B, 0x01,  //   Physical Maximum (315)
    0x65, 0x14,        //   Unit (Degrees)
    0x75, 0x04,        //   Report Size (4)
    0x95, 0x01,        //   Report Count (1)
    0x81, 0x42,        //   Input (Data, Var, Abs, Null State)
    0x75, 0x04,        //   Report Size (4) - padding
    0x95, 0x01,        //   Report Count (1)
    0x81, 0x03,        //   Input (Const, Var, Abs)

    // Left Stick X
    0x05, 0x01,        //   Usage Page (Generic Desktop)
    0x09, 0x30,        //   Usage (X)
    0x16, 0x01, 0x80,  //   Logical Minimum (-32767)
    0x26, 0xFF, 0x7F,  //   Logical Maximum (32767)
    0x75, 0x10,        //   Report Size (16)
    0x95, 0x01,        //   Report Count (1)
    0x81, 0x02,        //   Input (Data, Var, Abs)

    // Left Stick Y
    0x09, 0x31,        //   Usage (Y)
    0x81, 0x02,        //   Input (Data, Var, Abs)

    // Right Stick X (Z axis)
    0x09, 0x32,        //   Usage (Z)
    0x81, 0x02,        //   Input (Data, Var, Abs)

    // Right Stick Y (Rz axis)
    0x09, 0x35,        //   Usage (Rz)
    0x81, 0x02,        //   Input (Data, Var, Abs)

    // Left Trigger
    0x05, 0x02,        //   Usage Page (Simulation Controls)
    0x09, 0xC4,        //   Usage (Accelerator)
    0x15, 0x00,        //   Logical Minimum (0)
    0x26, 0xFF, 0x00,  //   Logical Maximum (255)
    0x75, 0x08,        //   Report Size (8)
    0x95, 0x01,        //   Report Count (1)
    0x81, 0x02,        //   Input (Data, Var, Abs)

    // Right Trigger
    0x09, 0xC5,        //   Usage (Brake)
    0x81, 0x02,        //   Input (Data, Var, Abs)

    0xC0               // End Collection
};

struct uhid_device {
    int fd;
};

static int uhid_create(int fd, const char *name, int player_index) {
    struct uhid_event ev;
    memset(&ev, 0, sizeof(ev));
    ev.type = UHID_CREATE2;

    snprintf((char *)ev.u.create2.name, sizeof(ev.u.create2.name),
             "%s %d", name, player_index);
    memcpy(ev.u.create2.rd_data, rdesc, sizeof(rdesc));
    ev.u.create2.rd_size = sizeof(rdesc);
    ev.u.create2.bus = BUS_USB;
    ev.u.create2.vendor = 0x1234;
    ev.u.create2.product = 0x5678;
    ev.u.create2.version = 1;
    ev.u.create2.country = 0;

    ssize_t ret = write(fd, &ev, sizeof(ev));
    if (ret < 0) {
        LOGE("UHID_CREATE2 write failed: %d", (int)ret);
        return -1;
    }
    return 0;
}

static int uhid_send_report(int fd, const uint8_t *report, size_t len) {
    struct uhid_event ev;
    memset(&ev, 0, sizeof(ev));
    ev.type = UHID_INPUT2;
    ev.u.input2.size = len;
    memcpy(ev.u.input2.data, report, len);

    ssize_t ret = write(fd, &ev, sizeof(ev));
    if (ret < 0) {
        LOGE("UHID_INPUT2 write failed: %d", (int)ret);
        return -1;
    }
    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_joegec_joycon2android_uhid_UhidGamepad_nativeCreate(
        JNIEnv *env, jobject thiz, jstring name, jint player_index) {

    int fd = open("/dev/uhid", O_RDWR);
    if (fd < 0) {
        LOGE("Failed to open /dev/uhid");
        return 0;
    }

    const char *name_str = (*env)->GetStringUTFChars(env, name, NULL);
    int ret = uhid_create(fd, name_str, player_index);
    (*env)->ReleaseStringUTFChars(env, name, name_str);

    if (ret < 0) {
        close(fd);
        return 0;
    }

    struct uhid_device *dev = malloc(sizeof(struct uhid_device));
    dev->fd = fd;
    return (jlong)(intptr_t)dev;
}

JNIEXPORT jlong JNICALL
Java_com_joegec_joycon2android_uhid_UhidGamepad_nativeCreateWithFd(
        JNIEnv *env, jobject thiz, jint fd, jstring name, jint player_index) {

    if (fd < 0) {
        LOGE("Invalid fd passed to nativeCreateWithFd");
        return 0;
    }

    const char *name_str = (*env)->GetStringUTFChars(env, name, NULL);
    int ret = uhid_create(fd, name_str, player_index);
    (*env)->ReleaseStringUTFChars(env, name, name_str);

    if (ret < 0) {
        return 0;
    }

    struct uhid_device *dev = malloc(sizeof(struct uhid_device));
    dev->fd = fd;
    return (jlong)(intptr_t)dev;
}

JNIEXPORT jboolean JNICALL
Java_com_joegec_joycon2android_uhid_UhidGamepad_nativeSendReport(
        JNIEnv *env, jobject thiz, jlong ptr, jbyteArray report) {

    if (ptr == 0) return JNI_FALSE;

    struct uhid_device *dev = (struct uhid_device *)(intptr_t)ptr;
    jsize len = (*env)->GetArrayLength(env, report);
    if (len != REPORT_SIZE) {
        LOGE("Invalid report size: %d (expected %d)", len, REPORT_SIZE);
        return JNI_FALSE;
    }

    uint8_t buf[REPORT_SIZE];
    (*env)->GetByteArrayRegion(env, report, 0, REPORT_SIZE, (jbyte *)buf);

    int ret = uhid_send_report(dev->fd, buf, REPORT_SIZE);
    return ret == 0 ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_joegec_joycon2android_uhid_UhidGamepad_nativeDestroy(
        JNIEnv *env, jobject thiz, jlong ptr) {

    if (ptr == 0) return;

    struct uhid_device *dev = (struct uhid_device *)(intptr_t)ptr;

    // Send UHID_DESTROY to cleanly remove the virtual device
    struct uhid_event ev;
    memset(&ev, 0, sizeof(ev));
    ev.type = UHID_DESTROY;
    write(dev->fd, &ev, sizeof(ev));

    close(dev->fd);
    free(dev);
}
