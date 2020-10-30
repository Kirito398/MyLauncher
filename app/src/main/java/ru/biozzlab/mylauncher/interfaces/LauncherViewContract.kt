package ru.biozzlab.mylauncher.interfaces

import androidx.lifecycle.LifecycleObserver
import ru.biozzlab.mylauncher.domain.models.ItemShortcut

interface LauncherViewContract {
    interface View {
        fun checkForLocaleChanged()
        fun setContentView()
        fun initViews()
        fun addShortcut(item: ItemShortcut)
    }

    interface Presenter : LifecycleObserver {
        fun setView(view: View)
        fun init()
    }
}