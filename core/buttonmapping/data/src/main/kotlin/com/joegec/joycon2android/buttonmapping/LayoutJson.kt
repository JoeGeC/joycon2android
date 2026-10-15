package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerNumber
import org.json.JSONArray
import org.json.JSONObject

private const val NAME = "name"
private const val CONSOLE = "console"
private const val SIDE = "side"
private const val SIDEWAYS_REMOTE = "sidewaysRemote"
private const val THRUST = "thrust"
private const val SENSITIVITY_PERCENT = "sensitivityPercent"
private const val BINDINGS = "bindings"
private const val BODIES = "bodies"
private const val PLAYER = "player"
private const val ENTRIES = "entries"
private const val FORMAT = "format"

// Format 2 stores the Pro Controller switch among the bindings; older documents predate it.
private const val GRIP_FORMAT = 2

internal fun SavedLayout.toJson(): String = JSONObject()
    .put(FORMAT, GRIP_FORMAT)
    .put(NAME, name)
    .put(CONSOLE, console.name)
    .put(SIDE, side.name)
    .put(SIDEWAYS_REMOTE, sidewaysRemote)
    .put(THRUST, thrust)
    .put(SENSITIVITY_PERCENT, sensitivityPercent)
    .put(BINDINGS, JSONObject(bindings))
    .toString()

/** Null for a document this build can no longer read, so one bad entry can't take the list with it. */
internal fun savedLayoutOf(id: String, json: String): SavedLayout? = runCatching {
    val document = JSONObject(json)
    val console = Console.valueOf(document.getString(CONSOLE))
    val side = JoyconSide.valueOf(document.getString(SIDE))
    SavedLayout(
        id = id,
        name = document.getString(NAME),
        console = console,
        side = side,
        bindings = document.getJSONObject(BINDINGS).toStringMap().upgraded(document, console, side),
        sidewaysRemote = document.optBoolean(SIDEWAYS_REMOTE),
        thrust = document.optBoolean(THRUST),
        sensitivityPercent = document.optInt(SENSITIVITY_PERCENT, RemoteMotion.DEFAULT_SENSITIVITY_PERCENT),
    )
}.getOrNull()

internal fun GlobalLayout.toJson(): String = JSONObject()
    .put(FORMAT, GRIP_FORMAT)
    .put(NAME, name)
    .put(CONSOLE, console.name)
    .put(BODIES, JSONArray(bodies.map { it.toJson() }))
    .toString()

internal fun globalLayoutOf(id: String, json: String): GlobalLayout? = runCatching {
    val document = JSONObject(json)
    val console = Console.valueOf(document.getString(CONSOLE))
    GlobalLayout(
        id = id,
        name = document.getString(NAME),
        console = console,
        bodies = document.getJSONArray(BODIES).objects().map { it.toSnapshot(document, console) },
    )
}.getOrNull()

private fun PlayerLayoutSnapshot.toJson() = JSONObject()
    .put(PLAYER, body.player.name)
    .put(SIDE, body.side.name)
    .put(SIDEWAYS_REMOTE, motion.sideways)
    .put(THRUST, motion.thrust)
    .put(SENSITIVITY_PERCENT, motion.sensitivityPercent)
    .put(ENTRIES, JSONObject(entries))

private fun JSONObject.toSnapshot(document: JSONObject, console: Console): PlayerLayoutSnapshot {
    val body = PlayerBody(PlayerNumber.valueOf(getString(PLAYER)), JoyconSide.valueOf(getString(SIDE)))
    return PlayerLayoutSnapshot(
        body = body,
        entries = getJSONObject(ENTRIES).toStringMap().upgraded(document, console, body.side),
        motion = motion(),
    )
}

private fun JSONObject.motion() = RemoteMotion(
    sideways = optBoolean(SIDEWAYS_REMOTE),
    thrust = optBoolean(THRUST),
    sensitivityPercent = optInt(SENSITIVITY_PERCENT, RemoteMotion.DEFAULT_SENSITIVITY_PERCENT),
)

private fun Map<String, String>.upgraded(document: JSONObject, console: Console, side: JoyconSide) =
    if (document.optInt(FORMAT) < GRIP_FORMAT) withLegacyGrip(console, side) else this

private fun JSONObject.toStringMap(): Map<String, String> =
    keys().asSequence().associateWith { getString(it) }

private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
