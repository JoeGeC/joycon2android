package com.joegec.joycon2android.dsu

class SetDsuLanUseCase(private val repository: DsuRepository) {
    operator fun invoke(enabled: Boolean) = repository.setLanEnabled(enabled)
}
