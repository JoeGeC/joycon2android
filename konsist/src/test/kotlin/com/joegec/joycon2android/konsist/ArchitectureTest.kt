package com.joegec.joycon2android.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/** Placement rules the module graph can't enforce: docs/architecture.md#dependency-rules */
class ArchitectureTest {

    @Test
    fun `view models live in a presentation or app module`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue {
                val path = it.containingFile.path
                path.contains("/presentation/") || path.contains("/app/")
            }
    }

    @Test
    fun `view models extend ViewModel`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { it.hasParentWithName("ViewModel", "AndroidViewModel") }
    }

    @Test
    fun `use cases live in a domain layer module`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue {
                val path = it.containingFile.path
                path.contains("/domain/") || path.contains("/core/session/")
            }
    }

    @Test
    fun `use cases are invoked through an invoke operator`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue { it.hasFunction { function -> function.name == "invoke" } }
    }

    @Test
    fun `repository abstractions are interfaces in a domain module`() {
        Konsist.scopeFromProject()
            .interfaces()
            .withNameEndingWith("Repository")
            .assertTrue { it.containingFile.path.contains("/domain/") }
    }
}
