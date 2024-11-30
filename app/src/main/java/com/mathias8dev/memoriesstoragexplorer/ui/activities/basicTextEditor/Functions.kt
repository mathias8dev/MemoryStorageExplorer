package com.mathias8dev.memoriesstoragexplorer.ui.activities.basicTextEditor

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader


fun readTextFromUri(context: Context, uri: Uri): String {
    return context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
    } ?: ""
}


