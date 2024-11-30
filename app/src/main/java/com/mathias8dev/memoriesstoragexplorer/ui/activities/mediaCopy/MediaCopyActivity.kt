package com.mathias8dev.memoriesstoragexplorer.ui.activities.mediaCopy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.mathias8dev.memoriesstoragexplorer.MainActivity

class MediaCopyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handle the intent and forward all data to MainActivity
        val originalIntent = intent

        // Create a new intent for MainActivity
        val intentToMain = Intent(this, MainActivity::class.java)
        intentToMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        // Forward the data URI (if available)
        originalIntent.data?.let { dataUri ->
            intentToMain.data = dataUri
        }

        // Forward extras (if available)
        originalIntent.extras?.let { extras ->
            intentToMain.putExtras(extras)
        }


        if (originalIntent.clipData != null) {
            intentToMain.clipData = originalIntent.clipData
        }

        // Add any other specific data processing (if needed)
        intentToMain.putExtra("INTERNAL_COPY_ACTION", true)

        // Start MainActivity
        startActivity(intentToMain)
        finish()

    }


}