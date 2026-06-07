package com.joegec.joycon2android.uhid

class UhidGamepad {

    companion object {
        init {
            System.loadLibrary("uhid_gamepad")
        }
    }

    private var nativePtr: Long = 0L

    val isCreated: Boolean get() = nativePtr != 0L

    fun create(name: String, playerIndex: Int): Boolean {
        nativePtr = nativeCreate(name, playerIndex)
        return nativePtr != 0L
    }

    fun createWithFd(fd: Int, name: String, playerIndex: Int): Boolean {
        nativePtr = nativeCreateWithFd(fd, name, playerIndex)
        return nativePtr != 0L
    }

    fun sendReport(report: ByteArray): Boolean {
        if (nativePtr == 0L) return false
        return nativeSendReport(nativePtr, report)
    }

    fun destroy() {
        if (nativePtr != 0L) {
            nativeDestroy(nativePtr)
            nativePtr = 0L
        }
    }

    private external fun nativeCreate(name: String, playerIndex: Int): Long
    private external fun nativeCreateWithFd(fd: Int, name: String, playerIndex: Int): Long
    private external fun nativeSendReport(ptr: Long, report: ByteArray): Boolean
    private external fun nativeDestroy(ptr: Long)
}
