package com.joegec.joycon2android.settings

class SetShowTipsUseCase(private val repository: TipsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setShowTips(enabled)
}
