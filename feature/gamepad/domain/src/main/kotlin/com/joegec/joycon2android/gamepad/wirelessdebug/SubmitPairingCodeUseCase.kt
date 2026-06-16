package com.joegec.joycon2android.gamepad.wirelessdebug

class SubmitPairingCodeUseCase(private val repository: WirelessDebugRepository) {
    operator fun invoke(code: String) = repository.submitPairingCode(code)
}
