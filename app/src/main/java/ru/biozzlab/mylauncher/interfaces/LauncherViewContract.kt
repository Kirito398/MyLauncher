package ru.biozzlab.mylauncher.interfaces

import androidx.lifecycle.LifecycleObserver

interface LauncherViewContract {
    interface View {
        fun checkForLocaleChanged()
        fun setContentView()
        fun initViews()
    }

    interface Presenter : LifecycleObserver {
        fun setView(view: View)
        fun init()
    }
}