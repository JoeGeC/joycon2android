package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoredControllerModelTest {

    @Test
    fun `every model the menu offers reads back as itself`() {
        (ControllerModel.entries - ControllerModel.UNKNOWN).forEach { model ->
            assertEquals(model, StoredControllerModel.parse(model.name))
        }
    }

    @Test
    fun `a side saved by an earlier build reads as no override`() {
        assertNull(StoredControllerModel.parse("PRO"))
        assertNull(StoredControllerModel.parse("LEFT"))
    }

    @Test
    fun `unknown, or anything that isn't a model name, reads as no override`() {
        assertNull(StoredControllerModel.parse("UNKNOWN"))
        assertNull(StoredControllerModel.parse(42))
    }
}
