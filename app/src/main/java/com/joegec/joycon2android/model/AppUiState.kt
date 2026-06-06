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
}
