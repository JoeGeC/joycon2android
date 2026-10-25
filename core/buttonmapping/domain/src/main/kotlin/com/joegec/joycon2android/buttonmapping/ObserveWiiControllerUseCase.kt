package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Falls back to the console's default layout. */
class ObserveWiiControllerUseCase(private val repository: WiiControllerRepository) {
    operator fun invoke(console: Console, body: PlayerBody): Flow<WiiController> =
        repository.observe(console, body).map { (it ?: MappingPresets.default(console).wiiController).heldBy(body.side) }
}
