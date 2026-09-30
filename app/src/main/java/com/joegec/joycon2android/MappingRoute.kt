package com.joegec.joycon2android

import com.joegec.joycon2android.buttonmapping.Console
import java.io.Serializable

data class MappingRoute(val console: Console, val fromDsu: Boolean) : Serializable
