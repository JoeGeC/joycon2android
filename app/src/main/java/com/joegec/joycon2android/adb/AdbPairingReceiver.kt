package com.joegec.joycon2android.adb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.joegec.joycon2android.service.Joycon2Service

/** Receives the pairing code typed into the notification and hands it to the service. */
class AdbPairingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val code = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(AdbPairingNotification.KEY_CODE)
            ?.toString()
            ?.trim()
            ?: return
        if (code.isEmpty()) return
        context.startService(
            Intent(context, Joycon2Service::class.java).apply {
                action = Joycon2Service.ACTION_ADB_PAIR_CODE
                putExtra(Joycon2Service.EXTRA_PAIR_CODE, code)
            },
        )
    }
}
