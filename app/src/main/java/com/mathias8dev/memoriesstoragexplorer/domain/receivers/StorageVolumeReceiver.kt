package com.mathias8dev.memoriesstoragexplorer.domain.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import timber.log.Timber

/**
 * BroadcastReceiver that listens for storage volume mount/unmount events
 * (USB drives, SD cards, etc.)
 */
class StorageVolumeReceiver(
    private val onStorageMounted: (path: String?) -> Unit,
    private val onStorageUnmounted: (path: String?) -> Unit,
    private val onStorageEjected: (path: String?) -> Unit,
    private val onStorageRemoved: (path: String?) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val path = intent.data?.path

        Timber.d("StorageVolumeReceiver: action=${intent.action}, path=$path")

        when (intent.action) {
            Intent.ACTION_MEDIA_MOUNTED -> {
                Timber.i("Storage mounted: $path")
                onStorageMounted(path)
            }
            Intent.ACTION_MEDIA_UNMOUNTED -> {
                Timber.i("Storage unmounted: $path")
                onStorageUnmounted(path)
            }
            Intent.ACTION_MEDIA_EJECT -> {
                Timber.i("Storage ejected: $path")
                onStorageEjected(path)
            }
            Intent.ACTION_MEDIA_REMOVED -> {
                Timber.i("Storage removed: $path")
                onStorageRemoved(path)
            }
            Intent.ACTION_MEDIA_BAD_REMOVAL -> {
                Timber.w("Storage bad removal: $path")
                onStorageRemoved(path)
            }
        }
    }

    companion object {
        /**
         * Creates an IntentFilter for all storage-related broadcast actions
         */
        fun createIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(Intent.ACTION_MEDIA_MOUNTED)
                addAction(Intent.ACTION_MEDIA_UNMOUNTED)
                addAction(Intent.ACTION_MEDIA_EJECT)
                addAction(Intent.ACTION_MEDIA_REMOVED)
                addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
                addDataScheme("file")
            }
        }

        /**
         * Registers the receiver with the given context
         */
        fun register(
            context: Context,
            receiver: StorageVolumeReceiver
        ) {
            val filter = createIntentFilter()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }
            Timber.d("StorageVolumeReceiver registered")
        }

        /**
         * Unregisters the receiver from the given context
         */
        fun unregister(context: Context, receiver: StorageVolumeReceiver) {
            try {
                context.unregisterReceiver(receiver)
                Timber.d("StorageVolumeReceiver unregistered")
            } catch (e: IllegalArgumentException) {
                Timber.w("Attempted to unregister receiver that was not registered")
            }
        }
    }
}
