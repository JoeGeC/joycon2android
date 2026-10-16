package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

class ObserveShowTipsUseCase(private val repository: TipsRepository) {
    operator fun invoke(): Flow<Boolean> = repository.showTips
}
