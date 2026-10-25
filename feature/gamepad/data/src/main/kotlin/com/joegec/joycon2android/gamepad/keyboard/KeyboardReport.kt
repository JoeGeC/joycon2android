package com.joegec.joycon2android.gamepad.keyboard

import com.joegec.joycon2android.model.AndroidKey

/** Laid out by [KeyboardDescriptor]: the keys report, then the Consumer report. */
object KeyboardReport {

    private const val KEYS_SIZE = 9
    private const val SLOTS_OFFSET = 3
    private const val SLOTS = 6

    fun build(frame: KeyboardFrame): List<ByteArray> = listOf(keys(frame), consumer(frame.androidKeys))

    private fun keys(frame: KeyboardFrame): ByteArray {
        val report = ByteArray(KEYS_SIZE)
        report[0] = KeyboardDescriptor.KEYS_REPORT_ID
        report[1] = frame.keys.mapNotNull(KeyboardUsages::modifierBit).fold(0) { acc, bit -> acc or (1 shl bit) }.toByte()
        val screenshot = if (AndroidKey.SCREENSHOT in frame.androidKeys) listOf(KeyboardUsages.PRINT_SCREEN) else emptyList()
        // Six held at once is the most a boot keyboard reports; the rest wait.
        (screenshot + frame.keys.mapNotNull(KeyboardUsages::usage)).take(SLOTS).forEachIndexed { slot, usage ->
            report[SLOTS_OFFSET + slot] = usage.toByte()
        }
        return report
    }

    private fun consumer(held: Set<AndroidKey>): ByteArray {
        val bits = (if (AndroidKey.HOME in held) 1 else 0) or (if (AndroidKey.BACK in held) 2 else 0)
        return byteArrayOf(KeyboardDescriptor.CONSUMER_REPORT_ID, bits.toByte())
    }
}
