package ru.biozzlab.mylauncher.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_launcher.*
import ru.biozzlab.mylauncher.App
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.controllers.DragController
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.biozzlab.mylauncher.presenters.LauncherPresenter
import ru.biozzlab.mylauncher.ui.views.DragLayer
import ru.biozzlab.mylauncher.ui.views.Workspace

class Launcher : AppCompatActivity(), LauncherViewContract.View {
    private lateinit var presenter: LauncherViewContract.Presenter
    lateinit var workspace: Workspace
    lateinit var dragController: DragController

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
        //workspace = workspaceView
        dragController = DragController(this)

        //dragLayer.setup(this, dragController)
    }

    //fun getDragLayer(): DragLayer = dragLayer
}