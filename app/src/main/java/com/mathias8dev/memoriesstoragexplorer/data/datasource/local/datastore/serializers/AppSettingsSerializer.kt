package com.mathias8dev.memoriesstoragexplorer.data.datasource.local.datastore.serializers

import androidx.datastore.core.Serializer
import com.google.gson.Gson
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter

class AppSettingsSerializer : Serializer<AppSettings> {
    private val gson = Gson()
    override val defaultValue: AppSettings
        get() = AppSettings()

    override suspend fun readFrom(input: InputStream): AppSettings {
        InputStreamReader(input).use { reader ->
            return gson.fromJson(reader, AppSettings::class.java)
        }
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        OutputStreamWriter(output).use { writer ->
            gson.toJson(t, AppSettings::class.java, writer)
        }
    }

    companion object {
        const val filename = "app-settings.json"
    }
}