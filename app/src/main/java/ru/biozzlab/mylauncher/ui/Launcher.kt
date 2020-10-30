package ru.biozzlab.mylauncher.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_launcher.*
import kotlinx.android.synthetic.main.item_hotseat.view.*
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.cache.CacheImpl
import ru.biozzlab.mylauncher.cache.RoomManager
import ru.biozzlab.mylauncher.controllers.DragController
import ru.biozzlab.mylauncher.data.RepositoryImpl
import ru.biozzlab.mylauncher.domain.interactor.LoadCells
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.biozzlab.mylauncher.presenters.LauncherPresenter
import ru.biozzlab.mylauncher.ui.layouts.params.CellLayoutParams
import ru.biozzlab.mylauncher.ui.views.IconDrawable
import ru.biozzlab.mylauncher.ui.views.Workspace

class Launcher : AppCompatActivity(), LauncherViewContract.View {
    private lateinit var presenter: LauncherViewContract.Presenter
    lateinit var workspace: Workspace
    lateinit var dragController: DragController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = LauncherPresenter(LoadCells(RepositoryImpl(CacheImpl(RoomManager.getClient(applicationContext)))))
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

    override fun addShortcut(item: ItemShortcut) {
        when (item.container) {
            ContainerType.HOT_SEAT -> addShortcutIntoHotSeat(item)
            ContainerType.DESKTOP -> TODO()
        }
    }

    private fun addShortcutIntoHotSeat(itemCell: ItemShortcut) {
        val layout = hotSeat.hotSeatContent
        val shortcut = createShortcut(layout, itemCell) ?: return
        val params = shortcut.layoutParams as CellLayoutParams

        params.cellX = itemCell.cellX
        params.cellY = itemCell.cellY
        params.cellHSpan = itemCell.cellHSpan
        params.cellVSpan = itemCell.cellVSpan

        hotSeat.hotSeatContent.addViewToCell(shortcut, -1, 0, params, false)
    }

    private fun createShortcut(parent: ViewGroup, item: ItemShortcut): View? {
        val view = layoutInflater.inflate(R.layout.item_application, parent, false) as TextView
        view.setOnClickListener { openApp(item.intent) }
        view.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null)
        view.text = item.title
        return view
    }

    private fun openApp(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: SecurityException) {
            e.printStackTrace()
            "App's not installed!".showToast()
        }
//        val launcherApp = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps? ?: return
//        item.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        launcherApp.startMainActivity(item.intent.component, item.user, item.intent.sourceBounds, null)
    }

    //fun getDragLayer(): DragLayer = dragLayer

    private fun String.showToast() {
        Toast.makeText(this@Launcher, this, Toast.LENGTH_SHORT).show()
    }
}