package ru.biozzlab.mylauncher.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.biozzlab.mylauncher.App
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.biozzlab.mylauncher.presenters.LauncherPresenter

class Launcher : AppCompatActivity(), LauncherViewContract.View {
    private lateinit var presenter: LauncherViewContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as App
        presenter = LauncherPresenter()
        presenter.setView(this)
        presenter.init()
    }

    override fun checkForLocaleChanged() {
        //TODO("Not yet implemented")
    }

    override fun setContentView() {
        setContentView(R.layout.activity_launcher)
    }

    override fun initViews() {
        TODO("Not yet implemented")
    }
}