package com.joegec.joycon2android.model

data class AppUiState(
    val scanning: Boolean = false,
    val error: String? = null,
    val unassignedJoycons: List<ConnectedJoycon> = emptyList(),
    val players: List<PlayerState> = PlayerNumber.entries.map { PlayerState(it) },
) {
    val anyConnected: Boolean
        get() = unassignedJoycons.isNotEmpty() || players.any { it.hasController }

    val activePlayers: List<PlayerState>
        get() = players.filter { it.hasController }

    /** Every controller's input, by address. */
    val liveInputs: Map<String, JoyconInput>
        get() = (unassignedJoycons + players.flatMap { listOfNotNull(it.left, it.right) })
            .associate { it.address to it.input }

    /** docs/architecture.md#live-input */
    fun withoutLiveInput(): AppUiState = copy(
        unassignedJoycons = unassignedJoycons.map(ConnectedJoycon::withoutLiveInput),
        players = players.map(PlayerState::withoutLiveInput),
    )
}
