package com.joegec.joycon2android.battery

/** Warns once per controller at each level it falls to; it re-arms above them all or on leaving. */
class LowBatteryWatch {
    private val warned = mutableMapOf<String, MutableSet<Int>>()

    fun newlyLow(batteries: List<ControllerBattery>, warningPercents: Set<Int>): List<ControllerBattery> {
        val reached = batteries.associate { battery ->
            battery.address to warningPercents.filterTo(mutableSetOf()) { battery.percent <= it }
        }
        warned.keys.retainAll { reached[it].orEmpty().isNotEmpty() }
        return batteries.filter { battery -> warnFor(battery.address, reached.getValue(battery.address)) }
    }

    fun reset() = warned.clear()

    // A controller first seen below both levels warns once, for the lower.
    private fun warnFor(address: String, reached: Set<Int>): Boolean {
        val lowest = reached.minOrNull() ?: return false
        val already = warned.getOrPut(address) { mutableSetOf() }
        val fresh = lowest !in already
        already += reached
        return fresh
    }
}
