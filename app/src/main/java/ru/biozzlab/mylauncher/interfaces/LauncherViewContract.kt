package ru.biozzlab.mylauncher.interfaces

import android.content.pm.PackageManager
import androidx.lifecycle.LifecycleObserver
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.biozzlab.mylauncher.domain.models.ItemWidget

interface LauncherViewContract {
    interface View {
        fun checkForLocaleChanged()
        fun setContentView()
        fun initViews()
        fun setListeners()
        fun addShortcut(item: ItemShortcut)
        fun addWidget(widget: ItemWidget)
        fun getPackageManager(): PackageManager
        fun setWorkspaceInitProgressBarVisibility(visible: Boolean)
    }

    interface Presenter : LifecycleObserver {
        fun setView(view: View)
        fun init()
        fun onItemShortcutDataChanged(item: ItemShortcut)
        fun addShortcutToUpdateQueue(item: ItemCell)
    }
}