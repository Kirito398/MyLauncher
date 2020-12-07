package ru.biozzlab.mylauncher.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_launcher.*
import kotlinx.android.synthetic.main.item_hotseat.view.*
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.cache.CacheImpl
import ru.biozzlab.mylauncher.cache.RoomManager
import ru.biozzlab.mylauncher.controllers.DragController
import ru.biozzlab.mylauncher.data.RepositoryImpl
import ru.biozzlab.mylauncher.domain.interactor.LoadCells
import ru.biozzlab.mylauncher.domain.interactor.UpdateShortcut
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.biozzlab.mylauncher.presenters.LauncherPresenter
import ru.biozzlab.mylauncher.ui.layouts.CellLayout
import ru.biozzlab.mylauncher.ui.layouts.DragLayer
import ru.biozzlab.mylauncher.ui.layouts.HotSeat
import ru.biozzlab.mylauncher.ui.layouts.params.CellLayoutParams
import ru.biozzlab.mylauncher.ui.layouts.Workspace

class Launcher : AppCompatActivity(), LauncherViewContract.View {
    private lateinit var presenter: LauncherViewContract.Presenter
    private lateinit var workspace: Workspace
    private lateinit var dragController: DragController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = RepositoryImpl(CacheImpl(RoomManager.getClient(applicationContext)))

        presenter = LauncherPresenter(LoadCells(repository), UpdateShortcut(repository))
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

        dragController = DragController()

        workspace.setup(dragController)
        workspace.setHotSeat(hotSeat as HotSeat)
        dragLayer.setup(dragController)
    }

    override fun setListeners() {
        workspace.setOnShortcutDataChangedListener { presenter.onItemShortcutDataChanged(it) }
    }

    override fun addShortcut(item: ItemShortcut) {
        val layout = when (item.container) {
            ContainerType.HOT_SEAT -> hotSeat.hotSeatContent
            ContainerType.DESKTOP -> workspace.getChildAt(item.desktopNumber) as CellLayout
        }

        val shortcut = createShortcut(layout, item) ?: return
        val params = shortcut.layoutParams as CellLayoutParams

        params.cellX = item.cellX
        params.cellY = item.cellY
        params.cellHSpan = item.cellHSpan
        params.cellVSpan = item.cellVSpan

        if (item.container == ContainerType.HOT_SEAT) params.showText = false

        layout.addViewToCell(shortcut, -1, item.id.toInt(), params, false)
    }

    private fun createShortcut(parent: ViewGroup, item: ItemShortcut): View? {
        val view = layoutInflater.inflate(R.layout.item_application, parent, false) as AppCompatTextView
        view.setOnClickListener { openApp(item.intent) }
        view.setOnLongClickListener { workspace.startDrag(it); return@setOnLongClickListener false }
        view.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null)

        val name = packageManager.getActivityInfo(ComponentName(item.packageName, item.className), 0).loadLabel(packageManager).toString()
        item.title = name

        view.text = item.title
        view.tag = item

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