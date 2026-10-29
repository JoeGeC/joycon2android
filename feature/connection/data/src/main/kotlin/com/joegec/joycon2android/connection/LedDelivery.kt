package com.joegec.joycon2android.connection

/** Resends the player LED until the controller echoes it: docs/protocol.md#player-leds */
internal class LedDelivery(
    private val scheduler: OpScheduler,
    private val send: () -> Unit,
) {
    private var retriesLeft = 0
    private val retry = Runnable { retryIfUnanswered() }

    @Synchronized
    fun deliver() {
        retriesLeft = MAX_ATTEMPTS - 1
        attempt()
    }

    @Synchronized
    fun onReply(reply: ByteArray) {
        if (!isLedEcho(reply)) return
        retriesLeft = 0
        scheduler.cancel(retry)
    }

    @Synchronized
    fun stop() {
        retriesLeft = 0
        scheduler.cancel(retry)
    }

    @Synchronized
    private fun retryIfUnanswered() {
        if (retriesLeft <= 0) return
        retriesLeft--
        attempt()
    }

    private fun attempt() {
        scheduler.cancel(retry)
        send()
        scheduler.postDelayed(retry, ECHO_TIMEOUT_MS)
    }

    private fun isLedEcho(reply: ByteArray) = reply.size > 3 &&
        reply[0] == LED_COMMAND && reply[1] == ControllerCommands.REPLY_MARKER && reply[3] == LED_SUBCOMMAND

    private companion object {
        const val LED_COMMAND: Byte = 0x09
        const val LED_SUBCOMMAND: Byte = 0x07

        // The echo takes about 50 ms.
        const val ECHO_TIMEOUT_MS = 300L
        const val MAX_ATTEMPTS = 5
    }
}
