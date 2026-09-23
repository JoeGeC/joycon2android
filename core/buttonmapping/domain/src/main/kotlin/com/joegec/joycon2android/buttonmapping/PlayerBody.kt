package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState

/** What every mapping is stored against. */
data class PlayerBody(val player: PlayerNumber, val side: JoyconSide)

/** Null while the player holds nothing. A Pro Controller has a pair's button set, so it maps as one. */
fun PlayerState.body(): PlayerBody? = joyconSide()?.let { PlayerBody(player, it) }

fun PlayerState.joyconSide(): JoyconSide? = when {
    hasPro || hasFullController -> JoyconSide.DUAL
    left != null -> JoyconSide.LEFT
    right != null -> JoyconSide.RIGHT
    else -> null
}
