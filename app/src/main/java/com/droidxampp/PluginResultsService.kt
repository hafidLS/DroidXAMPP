package com.droidxampp

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.droidxampp.termux.TermuxResultStore

class PluginResultsService : IntentService("DroidXAMPP-TermuxResults") {
    override fun onHandleIntent(intent: Intent?) {
        val bundle = intent?.getBundleExtra("result")
        if (bundle == null) {
            TermuxResultStore.add("Termux mengirim hasil tanpa result bundle.")
            return
        }
        val out = bundle.getString("stdout", "")
        val err = bundle.getString("stderr", "")
        val code = bundle.getInt("exitCode", -1)
        TermuxResultStore.add((out + if (err.isNotBlank()) "\n$err" else "").ifBlank { "Perintah selesai." } + "\nExit code: $code")
        Log.d("DroidXAMPP", "Termux command finished: $code")
    }
    companion object {
        const val EXTRA_ID = "execution_id"
        private var id = 1000
        @Synchronized fun nextId() = id++
    }
}
