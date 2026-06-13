package com.joegec.joycon2android.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Layer conventions the Gradle module graph cannot enforce on its own — *where* a kind of class
 * is allowed to live. The graph already stops presentation from seeing data; these stop a
 * ViewModel, use case, or repository interface from landing in the wrong layer in the first place.
 */
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
