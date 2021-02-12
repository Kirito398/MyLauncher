package ru.biozzlab.mylauncher.ui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.*

class PackageStatusChangeReceiver : BroadcastReceiver() {
    private var onDeletePackageListener: ((packageName: String) -> Unit)? = null
    private var onInstallPackageListener: ((packageName: String) -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val packageName = intent.data?.encodedSchemeSpecificPart ?: return

        when (action) {
            ACTION_PACKAGE_ADDED -> onInstallPackageListener?.invoke(packageName)
            ACTION_PACKAGE_FULLY_REMOVED -> onDeletePackageListener?.invoke(packageName)
        }
    }

    fun setOnInstallPackageListener(listener: (packageName: String) -> Unit) {
        onInstallPackageListener = listener
    }

    fun setOnDeletePackageListener(listener: (packageName: String) -> Unit) {
        onDeletePackageListener = listener
    }
}