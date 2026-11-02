package com.droidxampp

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.droidxampp.termux.TermuxResultStore

class PluginResultsService : IntentService("DroidXAMPP-TermuxResults") {

    override fun onHandleIntent(intent: Intent?) {
        val bundle = intent?.getBundleExtra("result")

        if (bundle == null) {
            TermuxResultStore.add("ERROR: Result bundle tidak ditemukan.")
            return
        }

        val stdout = bundle.getString("stdout", "").orEmpty()
        val stderr = bundle.getString("stderr", "").orEmpty()
        val exitCode = bundle.getInt("exitCode", -1)
        val errCode = bundle.getInt("err", Activity.RESULT_OK)
        val errMsg = bundle.getString("errmsg", "").orEmpty()

        val output = buildString {
            if (errCode != Activity.RESULT_OK) {
                appendLine("ERROR TERMUX: $errCode")
            }
            if (errMsg.isNotBlank()) appendLine(errMsg)
            if (stdout.isNotBlank()) appendLine(stdout)
            if (stderr.isNotBlank()) {
                appendLine("STDERR:")
                appendLine(stderr)
            }
            appendLine("Exit code: $exitCode")
        }

        TermuxResultStore.add(output)
        Log.d("DroidXAMPP", "Termux command finished: $exitCode")
    }

    companion object {
        const val EXTRA_ID = "execution_id"
        private var id = 1000

        @Synchronized
        fun nextId() = id++
    }
}
