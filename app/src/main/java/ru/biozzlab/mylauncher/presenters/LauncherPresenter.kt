package ru.biozzlab.mylauncher.presenters

import ru.biozzlab.mylauncher.interfaces.LauncherViewContract

class LauncherPresenter : LauncherViewContract.Presenter {
    private lateinit var view: LauncherViewContract.View

    override fun setView(view: LauncherViewContract.View) {
        this.view = view
    }

    override fun init() {
        view.checkForLocaleChanged()
        view.initViews()
    }
}