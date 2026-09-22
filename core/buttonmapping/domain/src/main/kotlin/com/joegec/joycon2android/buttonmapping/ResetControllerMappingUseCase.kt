package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPresets

/** Back to the console's own layout, whatever the body had become. */
class ResetControllerMappingUseCase(private val applyMappingLayout: ApplyMappingLayoutUseCase) {
    suspend operator fun invoke(console: Console, body: PlayerBody) =
        applyMappingLayout(console, body, MappingPresets.default(console).id)
}
