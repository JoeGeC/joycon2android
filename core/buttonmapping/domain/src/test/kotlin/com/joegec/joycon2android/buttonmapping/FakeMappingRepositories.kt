package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private typealias BodyKey = Pair<Console, PlayerBody>

internal class FakeControllerMappings : ControllerMappingRepository {
    private val stored = MutableStateFlow(emptyMap<BodyKey, Map<String, String>>())

    override fun observe(console: Console, body: PlayerBody): Flow<Map<String, String>> =
        stored.map { it[console to body].orEmpty() }

    override suspend fun set(console: Console, body: PlayerBody, targetKey: String, sourceId: String) {
        stored.update { it + ((console to body) to (it[console to body].orEmpty() + (targetKey to sourceId))) }
    }

    override suspend fun replace(console: Console, body: PlayerBody, entries: Map<String, String>) {
        stored.update { it + ((console to body) to entries) }
    }
}

internal class FakeSidewaysRemotes : SidewaysRemoteRepository {
    private val stored = MutableStateFlow(emptyMap<BodyKey, Boolean>())

    override fun observe(console: Console, body: PlayerBody): Flow<Boolean?> = stored.map { it[console to body] }

    override suspend fun set(console: Console, body: PlayerBody, enabled: Boolean) {
        stored.update { it + ((console to body) to enabled) }
    }
}

internal class FakeSavedLayouts(vararg initial: SavedLayout) : SavedLayoutRepository {
    private val stored = MutableStateFlow(initial.associateBy { it.id })

    override fun observe(): Flow<List<SavedLayout>> = stored.map { it.values.toList() }

    override suspend fun save(layout: SavedLayout) {
        stored.update { it + (layout.id to layout) }
    }

    override suspend fun delete(layoutId: String) {
        stored.update { it - layoutId }
    }
}

internal class FakeGlobalLayouts(vararg initial: GlobalLayout) : GlobalLayoutRepository {
    private val stored = MutableStateFlow(initial.associateBy { it.id })

    override fun observe(): Flow<List<GlobalLayout>> = stored.map { it.values.toList() }

    override suspend fun save(layout: GlobalLayout) {
        stored.update { it + (layout.id to layout) }
    }

    override suspend fun delete(layoutId: String) {
        stored.update { it - layoutId }
    }
}
