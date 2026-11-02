package com.droidxampp.termux

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.droidxampp.PluginResultsService

object TermuxCommandRunner {

    fun run(
        context: Context,
        service: Service,
        action: Action,
        onSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val intent = Intent(TermuxCommand.RUN_ACTION).apply {
                setClassName(
                    TermuxCommand.TERMUX_PKG,
                    "com.termux.app.RunCommandService"
                )

                putExtra(TermuxCommand.EXTRA_PATH, TermuxCommand.BASH)

                putExtra(
                    TermuxCommand.EXTRA_ARGS,
                    arrayOf(
                        TermuxCommand.scriptPath(service),
                        action.arg
                    )
                )

                putExtra(TermuxCommand.EXTRA_WORKDIR, TermuxCommand.HOME)
                putExtra(TermuxCommand.EXTRA_BACKGROUND, true)

                val request = PluginResultsService.nextId()

                val resultIntent = Intent(
                    context,
                    PluginResultsService::class.java
                ).putExtra(
                    PluginResultsService.EXTRA_ID,
                    request
                )

                val flags = PendingIntent.FLAG_ONE_SHOT or
                    if (Build.VERSION.SDK_INT >= 31) {
                        PendingIntent.FLAG_MUTABLE
                    } else {
                        0
                    }

                val pending = PendingIntent.getService(
                    context,
                    request,
                    resultIntent,
                    flags
                )

                putExtra(TermuxCommand.EXTRA_PENDING_INTENT, pending)
            }

            val started = context.startService(intent)

            if (started == null) {
                onError("Termux tidak menerima perintah. Periksa instalasi dan service Termux.")
            } else {
                onSent()
            }

        } catch (e: Exception) {
            onError(
                e.message ?: "Gagal mengirim perintah ke Termux."
            )
        }
    }
}
