package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.dsu.DsuSlots
import com.joegec.joycon2android.emulatorconfig.IniEditor

// A remote an earlier setup left on our server reads whatever now streams on its slot, like a pair's second hand.
internal object StaleDolphinWiimotes {
    private val OUR_DEVICE = Regex("""DSUClient/\d+/Joycon2""")

    fun disconnected(existing: String?, written: Set<String>): Map<String, String> =
        (1..DsuSlots.COUNT).map { "[Wiimote$it]" }
            .filter { it !in written && readsOurServer(existing, it) }
            .associateWith { "Source = 0\n" }

    private fun readsOurServer(existing: String?, section: String) =
        IniEditor.valueOf(existing, section, "Device")?.let(OUR_DEVICE::matches) == true
}
