package com.joegec.joycon2android.buttonmapping

data class SavedLayout(
    override val id: String,
    val name: String,
    val console: Console,
    val side: JoyconSide,
    val bindings: Map<String, String>,
    override val wiiController: WiiController = WiiController.REMOTE,
    override val thrust: Boolean = false,
    override val sensitivityPercent: Int = RemoteMotion.DEFAULT_SENSITIVITY_PERCENT,
) : MappingLayout {
    /** Only ever offered back to the body it was saved from, so [side] is always that one. */
    override fun entries(side: JoyconSide) = bindings
}
