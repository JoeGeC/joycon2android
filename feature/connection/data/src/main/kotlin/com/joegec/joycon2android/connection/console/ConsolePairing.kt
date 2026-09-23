package com.joegec.joycon2android.connection.console

import android.annotation.SuppressLint
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/** Report 0x15, which stores this host on the controller: docs/protocol.md#pairing */
internal object ConsolePairing {

    val A1: ByteArray = hex("3503e92982877124bea80c664615834b")
    val A2: ByteArray = hex("6fc6df8ad8fedf15bb8c15e91f320544")
    private val KNOWN_B1: ByteArray = hex("5cf6ee792cdf05e1ba2b6325c41a5f10")

    private const val KEY_LENGTH = 16
    private val ADDRESS_PATTERN = Regex("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$")

    /** `AA:BB:CC:DD:EE:FF` as the controller expects it: byte-reversed. Null when malformed. */
    fun encodeAddress(address: String): ByteArray? {
        if (!ADDRESS_PATTERN.matches(address)) return null
        return address.split(":").map { it.toInt(16).toByte() }.reversed().toByteArray()
    }

    fun secondAddress(encoded: ByteArray): ByteArray =
        encoded.copyOf().also { it[0] = (it[0] - 1).toByte() }

    /** B1 from the 0x15/0x04 reply data (status byte, then the key). */
    fun b1From(replyData: ByteArray?): ByteArray =
        replyData?.takeIf { it.size > KEY_LENGTH }?.copyOfRange(1, KEY_LENGTH + 1) ?: KNOWN_B1

    fun ltk(b1: ByteArray): ByteArray = ByteArray(KEY_LENGTH) { (A1[it].toInt() xor b1[it].toInt()).toByte() }

    // The controller computes B2 with single-block AES-ECB; the mode is fixed by the protocol.
    @SuppressLint("GetInstance")
    fun confirms(ltk: ByteArray, replyData: ByteArray?): Boolean {
        if (replyData == null || replyData.size <= KEY_LENGTH) return false
        val expected = Cipher.getInstance("AES/ECB/NoPadding").run {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(ltk.reversedArray(), "AES"))
            doFinal(A2.reversedArray())
        }
        return replyData.copyOfRange(1, KEY_LENGTH + 1).contentEquals(expected)
    }

    private fun hex(digits: String): ByteArray =
        ByteArray(digits.length / 2) { digits.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
