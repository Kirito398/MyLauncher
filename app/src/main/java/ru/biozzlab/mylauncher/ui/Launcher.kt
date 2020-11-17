package ru.biozzlab.mylauncher.ui

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ComponentInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_launcher.*
import kotlinx.android.synthetic.main.activity_launcher.view.*
import kotlinx.android.synthetic.main.item_cell.view.*
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
import ru.biozzlab.mylauncher.ui.layouts.CellLayout
import ru.biozzlab.mylauncher.ui.layouts.DragLayer
import ru.biozzlab.mylauncher.ui.layouts.params.CellLayoutParams
import ru.biozzlab.mylauncher.ui.layouts.Workspace

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
        workspace = workspaceView

        //workspace.cell1.setBackgroundColor(resources.getColor(R.color.colorAccent))

        dragController = DragController(this)

        dragLayer.setup(this, dragController)
    }

    override fun addShortcut(item: ItemShortcut) {
        when (item.container) {
            ContainerType.HOT_SEAT -> addShortcutIntoHotSeat(item)
            ContainerType.DESKTOP -> addShortcutIntoDesktop(item)
        }
    }

    private fun addShortcutIntoDesktop(item: ItemShortcut) {
        val cell = workspace.getChildAt(0) as CellLayout
        val shortcut = createShortcut(cell, item) ?: return
        val params = shortcut.layoutParams as CellLayoutParams

        params.cellX = item.cellX
        params.cellY = item.cellY
        params.cellHSpan = item.cellHSpan
        params.cellVSpan = item.cellVSpan

        cell.addViewToCell(shortcut, -1, 0, params, false)
        workspace.requestLayout()
    }

    private fun addShortcutIntoHotSeat(itemCell: ItemShortcut) {
        val layout = hotSeat.hotSeatContent
        val shortcut = createShortcut(layout, itemCell) ?: return
        val params = shortcut.layoutParams as CellLayoutParams

        params.cellX = itemCell.cellX
        params.cellY = itemCell.cellY
        params.cellHSpan = itemCell.cellHSpan
        params.cellVSpan = itemCell.cellVSpan

        (shortcut as AppCompatTextView).setTextColor(ContextCompat.getColor(applicationContext, R.color.hot_seat_text_color))

        hotSeat.hotSeatContent.addViewToCell(shortcut, -1, 0, params, false)
    }

    private fun createShortcut(parent: ViewGroup, item: ItemShortcut): View? {
        val view = layoutInflater.inflate(R.layout.item_application, parent, false) as AppCompatTextView
        view.setOnClickListener { openApp(item.intent) }
        view.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null)

        val name = packageManager.getActivityInfo(ComponentName(item.packageName, item.className), 0).loadLabel(packageManager).toString()
        item.title = name

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

    fun getDragLayer(): DragLayer = dragLayer

    private fun String.showToast() {
        Toast.makeText(this@Launcher, this, Toast.LENGTH_SHORT).show()
    }
}