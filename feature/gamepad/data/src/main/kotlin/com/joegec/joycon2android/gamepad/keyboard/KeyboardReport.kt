package com.joegec.joycon2android.gamepad.keyboard

import com.joegec.joycon2android.model.AndroidKey

/** Laid out by [KeyboardDescriptor]: the keys report, then the Consumer report. */
object KeyboardReport {

    private const val KEYS_SIZE = 9
    private const val KEYS_OFFSET = 3
    private const val PRINT_SCREEN: Byte = 0x46

    fun build(held: Set<AndroidKey>): List<ByteArray> = listOf(keys(held), consumer(held))

    private fun keys(held: Set<AndroidKey>): ByteArray {
        val report = ByteArray(KEYS_SIZE)
        report[0] = KeyboardDescriptor.KEYS_REPORT_ID
        if (AndroidKey.SCREENSHOT in held) report[KEYS_OFFSET] = PRINT_SCREEN
        return report
    }

    private fun consumer(held: Set<AndroidKey>): ByteArray {
        val bits = (if (AndroidKey.HOME in held) 1 else 0) or (if (AndroidKey.BACK in held) 2 else 0)
        return byteArrayOf(KeyboardDescriptor.CONSUMER_REPORT_ID, bits.toByte())
    }
}
