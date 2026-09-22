package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerNumber
import org.json.JSONArray
import org.json.JSONObject

private const val NAME = "name"
private const val CONSOLE = "console"
private const val SIDE = "side"
private const val SIDEWAYS_REMOTE = "sidewaysRemote"
private const val BINDINGS = "bindings"
private const val BODIES = "bodies"
private const val PLAYER = "player"
private const val ENTRIES = "entries"

internal fun SavedLayout.toJson(): String = JSONObject()
    .put(NAME, name)
    .put(CONSOLE, console.name)
    .put(SIDE, side.name)
    .put(SIDEWAYS_REMOTE, sidewaysRemote)
    .put(BINDINGS, JSONObject(bindings))
    .toString()

/** Null for a document this build can no longer read, so one bad entry can't take the list with it. */
internal fun savedLayoutOf(id: String, json: String): SavedLayout? = runCatching {
    val document = JSONObject(json)
    SavedLayout(
        id = id,
        name = document.getString(NAME),
        console = Console.valueOf(document.getString(CONSOLE)),
        side = JoyconSide.valueOf(document.getString(SIDE)),
        bindings = document.getJSONObject(BINDINGS).toStringMap(),
        sidewaysRemote = document.optBoolean(SIDEWAYS_REMOTE),
    )
}.getOrNull()

internal fun GlobalLayout.toJson(): String = JSONObject()
    .put(NAME, name)
    .put(CONSOLE, console.name)
    .put(BODIES, JSONArray(bodies.map { it.toJson() }))
    .toString()

internal fun globalLayoutOf(id: String, json: String): GlobalLayout? = runCatching {
    val document = JSONObject(json)
    GlobalLayout(
        id = id,
        name = document.getString(NAME),
        console = Console.valueOf(document.getString(CONSOLE)),
        bodies = document.getJSONArray(BODIES).objects().map { it.toSnapshot() },
    )
}.getOrNull()

private fun PlayerLayoutSnapshot.toJson() = JSONObject()
    .put(PLAYER, body.player.name)
    .put(SIDE, body.side.name)
    .put(SIDEWAYS_REMOTE, sidewaysRemote)
    .put(ENTRIES, JSONObject(entries))

private fun JSONObject.toSnapshot() = PlayerLayoutSnapshot(
    body = PlayerBody(PlayerNumber.valueOf(getString(PLAYER)), JoyconSide.valueOf(getString(SIDE))),
    entries = getJSONObject(ENTRIES).toStringMap(),
    sidewaysRemote = optBoolean(SIDEWAYS_REMOTE),
)

private fun JSONObject.toStringMap(): Map<String, String> =
    keys().asSequence().associateWith { getString(it) }

private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
