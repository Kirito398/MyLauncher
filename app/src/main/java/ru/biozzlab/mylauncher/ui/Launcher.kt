package ru.biozzlab.mylauncher.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.android.synthetic.main.activity_launcher.*
import kotlinx.android.synthetic.main.item_hotseat.view.*
import ru.biozzlab.mylauncher.App
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.controllers.DragController
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.biozzlab.mylauncher.domain.models.ItemWidget
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.biozzlab.mylauncher.ui.layouts.CellLayout
import ru.biozzlab.mylauncher.ui.layouts.DragLayer
import ru.biozzlab.mylauncher.ui.layouts.HotSeat
import ru.biozzlab.mylauncher.ui.layouts.params.CellLayoutParams
import ru.biozzlab.mylauncher.ui.layouts.Workspace
import ru.biozzlab.mylauncher.ui.widgets.LauncherAppWidgetHost
import javax.inject.Inject

class Launcher : AppCompatActivity(), LauncherViewContract.View {
    @Inject
    lateinit var presenter: LauncherViewContract.Presenter

    private lateinit var workspace: Workspace
    private lateinit var dragController: DragController
    private lateinit var appWidgetHost: LauncherAppWidgetHost
    private lateinit var appWidgetManager: AppWidgetManager

    private var addingWidgetQueue = mutableMapOf<Int, ItemWidget>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App.appComponent.injectLauncher(this)

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

        appWidgetHost = LauncherAppWidgetHost(applicationContext, 1024)
        appWidgetManager = AppWidgetManager.getInstance(applicationContext)
    }

    override fun setListeners() {
        workspace.setOnShortcutDataChangedListener { presenter.onItemShortcutDataChanged(it) }
        workspace.setOnShortcutLongPressListener { openDeleteAppDialog(it.tag as ItemShortcut) }
        (hotSeat as HotSeat).setOnAllAppsButtonClickListener { selectWidget() }
    }

    override fun addShortcut(item: ItemShortcut) {
        if (item.desktopNumber < 0 || item.cellX < 0 || item.cellY < 0)
            findAreaInCellLayout(item)

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

    override fun setWorkspaceInitProgressBarVisibility(visible: Boolean) {
        pbWorkspaceInit.visibility = if (visible) View.VISIBLE else View.GONE
        workspace.visibility = if (!visible) View.VISIBLE else View.GONE
    }

    private fun findAreaInCellLayout(item: ItemCell) {
        val position = mutableListOf(-1, -1)
        val desktopNumber = workspace.findEmptyArea(position, item.cellHSpan, item.cellVSpan)

        item.desktopNumber = desktopNumber
        item.cellX = position[0]
        item.cellY = position[1]

        presenter.addShortcutToUpdateQueue(item)
    }

    private fun createShortcut(parent: ViewGroup, item: ItemShortcut): View? {
        val view = layoutInflater.inflate(R.layout.item_application, parent, false)
                as? AppCompatTextView ?: return null

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

    private fun openDeleteAppDialog(item: ItemShortcut) {
        val packageURI = Uri.parse("package:${item.packageName}")
        val intent = Intent(Intent.ACTION_DELETE, packageURI)
        startActivity(intent)
    }

    fun getDragLayer(): DragLayer = dragLayer

    private fun String.showToast() {
        Toast.makeText(this@Launcher, this, Toast.LENGTH_SHORT).show()
    }

    /**-------Работа с виджетами-------*/
    companion object {
        private const val REQUEST_CREATE_APPWIDGET = 2
        private const val REQUEST_BIND_APPWIDGET = 3
        private const val REQUEST_PICK_APPWIDGET = 4
        private const val EXTRA_APPWIDGET_MODEL = "extra_appwidget_model"
    }

    override fun addWidget(widget: ItemWidget) {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        addingWidgetQueue.put(appWidgetId, widget)

        var widgetInfo: AppWidgetProviderInfo? = null
        for (widgetProvider in appWidgetManager.installedProviders) {
            if (widgetProvider.provider.className != widget.className) continue
            widgetInfo = widgetProvider
            break
        }

        val widgetProvider = widgetInfo?.provider ?: return

        if (appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, widgetProvider)) {
            createWidget(appWidgetId, widget)
        } else {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, widgetProvider)
                //putExtra(EXTRA_APPWIDGET_MODEL, widget)
            }

            startActivityForResult(intent, REQUEST_BIND_APPWIDGET)
        }
    }

    private fun selectWidget() {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_BIND_APPWIDGET) {
                data?.extras?.let {
                    //val widget = it.getParcelable<ItemWidget>(EXTRA_APPWIDGET_MODEL) ?: return@let
                    val appWidgetId = it.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
                    val widget = addingWidgetQueue[appWidgetId] ?: return@let
                    addingWidgetQueue.remove(appWidgetId)

                    createWidget(appWidgetId, widget)
                }
            }
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                data?.extras?.let {
                    val appWidgetId = it.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
                    configureWidget(appWidgetId)
                }
            }
            if (requestCode == REQUEST_CREATE_APPWIDGET) {
                data?.extras?.let {
                    val appWidgetId = it.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
                    createWidget(appWidgetId)
                }
            }
        }
    }

    private fun createWidget(appWidgetId: Int, widget: ItemWidget? = null) {
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

        val item = widget
            ?: ItemCell(
                type = WorkspaceItemType.WIDGET,
                container = ContainerType.DESKTOP,
                packageName = appWidgetInfo.provider.packageName,
                className = appWidgetInfo.provider.className,
                cellHSpan = -1,
                cellVSpan = -1
            )

        if (item.cellHSpan < 0 || item.cellVSpan < 0)
            (workspace.getChildAt(0) as CellLayout).calculateItemDimensions(item, appWidgetInfo.minHeight, appWidgetInfo.minWidth)
            //(workspace.getChildAt(0) as CellLayout).calculateItemDimensions(item, maxHeight, maxWidth)

        if (item.desktopNumber < 0 || item.cellX < 0 || item.cellY < 0)
            findAreaInCellLayout(item)

        val layout = workspace.getChildAt(item.desktopNumber) as CellLayout
        val widgetView = appWidgetHost.createView(applicationContext, appWidgetId, appWidgetInfo)
        widgetView.setAppWidget(appWidgetId, appWidgetInfo)
        val params = CellLayoutParams(item.cellX, item.cellY, item.cellHSpan, item.cellVSpan)
        widgetView.layoutParams = params
        widgetView.tag = item

        widgetView.setOnLongClickListener {
            workspace.startDrag(it)
            return@setOnLongClickListener false
        }

        layout.addViewToCell(widgetView, -1, 30, params, false)
    }

    private fun configureWidget(appWidgetId: Int) {
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

        if (appWidgetInfo.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
            intent.component = appWidgetInfo.configure
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET)
        } else {
            createWidget(appWidgetId)
        }
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }
}