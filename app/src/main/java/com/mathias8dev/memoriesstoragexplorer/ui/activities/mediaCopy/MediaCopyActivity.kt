package com.mathias8dev.memoriesstoragexplorer.ui.activities.mediaCopy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.mathias8dev.memoriesstoragexplorer.MainActivity
import timber.log.Timber

class MediaCopyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handle the intent and forward all data to MainActivity
        val originalIntent = intent

        Timber.d("MediaCopyActivity received intent: action=${originalIntent.action}, data=${originalIntent.data}, clipData=${originalIntent.clipData}")

        // Create a new intent for MainActivity
        val intentToMain = Intent(this, MainActivity::class.java)
        intentToMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        // IMPORTANT: Forward the original action so MainActivity knows what to do
        intentToMain.action = originalIntent.action

        // Forward the data URI (if available)
        originalIntent.data?.let { dataUri ->
            intentToMain.data = dataUri
            Timber.d("Forwarding data URI: $dataUri")
        }

        // Forward the type
        originalIntent.type?.let { type ->
            intentToMain.type = type
            Timber.d("Forwarding type: $type")
        }

        // Forward extras (if available)
        originalIntent.extras?.let { extras ->
            intentToMain.putExtras(extras)
        }

        // Forward clipData (for SEND_MULTIPLE or multiple items)
        if (originalIntent.clipData != null) {
            intentToMain.clipData = originalIntent.clipData
            Timber.d("Forwarding clipData with ${originalIntent.clipData!!.itemCount} items")
        }

        // Mark this as an internal copy action for MainActivity to handle
        intentToMain.putExtra("INTERNAL_COPY_ACTION", true)

        // Start MainActivity
        startActivity(intentToMain)
        finish()
    }
}