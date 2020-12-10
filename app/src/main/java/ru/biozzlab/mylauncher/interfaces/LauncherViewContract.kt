package ru.biozzlab.mylauncher.interfaces

import android.content.pm.PackageManager
import androidx.lifecycle.LifecycleObserver
import ru.biozzlab.mylauncher.domain.models.ItemShortcut

interface LauncherViewContract {
    interface View {
        fun checkForLocaleChanged()
        fun setContentView()
        fun initViews()
        fun setListeners()
        fun addShortcut(item: ItemShortcut)
        fun getPackageManager(): PackageManager
    }

    interface Presenter : LifecycleObserver {
        fun setView(view: View)
        fun init()
        fun onItemShortcutDataChanged(item: ItemShortcut)
    }
}