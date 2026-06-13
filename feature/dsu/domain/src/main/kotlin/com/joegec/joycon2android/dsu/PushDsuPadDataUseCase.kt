package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.PlayerState

class PushDsuPadDataUseCase(private val repository: DsuRepository) {
    operator fun invoke(players: List<PlayerState>) = repository.push(players)
}
