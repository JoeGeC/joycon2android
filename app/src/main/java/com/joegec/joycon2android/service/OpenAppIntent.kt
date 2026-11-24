package com.joegec.joycon2android.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.joegec.joycon2android.MainActivity

fun openAppIntent(context: Context): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
}
