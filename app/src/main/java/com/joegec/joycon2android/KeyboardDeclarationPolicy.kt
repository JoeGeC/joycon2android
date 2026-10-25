package com.joegec.joycon2android

import com.joegec.joycon2android.model.KeyboardKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** Tells the keyboard every key a binding can press, so it declares letters only while one is bound. */
class KeyboardDeclarationPolicy(
    private val scope: CoroutineScope,
    private val bound: Flow<Set<KeyboardKey>>,
    private val declare: (Set<KeyboardKey>) -> Unit,
) {

    fun start() {
        scope.launch { bound.distinctUntilChanged().collect(declare) }
    }
}
