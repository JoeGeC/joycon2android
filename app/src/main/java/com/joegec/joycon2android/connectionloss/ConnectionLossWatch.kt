package com.joegec.joycon2android.connectionloss

/** Reports each drop once, and its controller's return; one removed while lost is forgotten. */
class ConnectionLossWatch {
    private val lost = mutableSetOf<String>()

    fun update(lostNow: List<LostController>, connected: Set<String>): ConnectionLossChange {
        val fresh = lostNow.filter { it.address !in lost }
        val returned = lost.filterTo(mutableSetOf()) { it in connected }
        lost.clear()
        lostNow.mapTo(lost) { it.address }
        return ConnectionLossChange(fresh, returned)
    }
}
