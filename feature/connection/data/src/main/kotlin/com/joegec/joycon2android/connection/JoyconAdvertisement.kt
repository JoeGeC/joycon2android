import android.util.Log

/**
 * Joy-Con 2 advertisements carry the bonded host's MAC to signal wake vs pairing mode.
 * - ID 0x0553 (Nintendo): MAC at bytes [10..15]
 * - ID 0x75 (Nyxi): MAC at bytes [5..10]
 * Holding SYNC (pairing mode) zeroes this field.
 */
object JoyconAdvertisement {

    private const val TAG = "Joycon2"
    private const val HOST_MAC_LENGTH = 6

    /** True when the controller is open for pairing rather than waking for its bonded host. */
    fun isPairing(id: Int, manufacturerData: ByteArray): Boolean {
        val offset = when (id) {
            0x0442 -> 3
            0x6c42 -> 0
            0x0553 -> 10
            else -> return true
        }

        // For Nyxi/Keylinker (0x6c42), if data is too short, treat as pairing.
        if (id == 0x6c42 && manufacturerData.size < offset + HOST_MAC_LENGTH) {
            return true
        }

        if (manufacturerData.size < offset + HOST_MAC_LENGTH) return true

        val macSlice = manufacturerData.sliceArray(offset until offset + HOST_MAC_LENGTH)
        val isPairing = macSlice.all { it == 0.toByte() }
        
        Log.d(TAG, "isPairing check: id=0x${Integer.toHexString(id)}, offset=$offset, data=${macSlice.joinToString("") { "%02X".format(it) }} -> $isPairing")

        return isPairing
    }
}
