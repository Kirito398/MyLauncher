package ru.biozzlab.mylauncher.ui.fragments

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.controllers.DragController
import ru.biozzlab.mylauncher.databinding.FragmentDesktopBinding
import ru.biozzlab.mylauncher.di.components.AppComponent
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.biozzlab.mylauncher.domain.models.ItemWidget
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType
import ru.biozzlab.mylauncher.easyLog
import ru.biozzlab.mylauncher.ui.layouts.CellLayout
import ru.biozzlab.mylauncher.ui.layouts.Workspace
import ru.biozzlab.mylauncher.ui.layouts.params.CellLayoutParams
import ru.biozzlab.mylauncher.ui.widgets.LauncherAppWidgetHost
import ru.biozzlab.mylauncher.view_models.DesktopViewModel
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.extensions.launchWhenStarted
import ru.sir.presentation.extensions.showToast

class Desktop : BaseFragment<DesktopViewModel, FragmentDesktopBinding>(DesktopViewModel::class.java) {

    private lateinit var workspace: Workspace

    private lateinit var appWidgetHost: LauncherAppWidgetHost
    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var packageManager: PackageManager

    private lateinit var applicationContext: Context

    override fun inject(app: BaseApplication) {
        app.getComponent<AppComponent>().injectDesktop(this)
        applicationContext = app
    }

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentDesktopBinding = FragmentDesktopBinding.inflate(inflater, container, false)

    override fun initVars() {
        workspace = binding.workspaceView

        val dragController = DragController()
        workspace.setup(dragController, binding.hotSeat.root, binding.deleteRegion.root)
        binding.dragLayer.setup(dragController)

        appWidgetHost = LauncherAppWidgetHost(applicationContext, 1024)
        appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        packageManager = requireActivity().packageManager

        viewModel.workspaceItems.launchWhenStarted(lifecycleScope) {
            it.forEach { item ->
                when (item.type) {
                    WorkspaceItemType.SHORTCUT -> addShortcut(ItemShortcut(item))
                    WorkspaceItemType.WIDGET -> addWidget(ItemWidget(item))
                }
            }
            viewModel.updateDesktop()
        }

        viewModel.removedItems.launchWhenStarted(lifecycleScope) {
            it.forEach { item -> workspace.removeViewWithPackages(item.packageName) }
        }
    }

    override fun setListeners() {
        workspace.setOnItemCellDataChangedListener { viewModel.onItemCellDataChanged(it) }
        binding.hotSeat.root.setOnAllAppsButtonClickListener { selectWidget() }

        workspace.setOnItemDeleteListener {
            when (it) {
                is ItemShortcut -> openDeleteAppDialog(it)
                is ItemWidget -> deleteWidget(it)
            }
        }
    }

    private fun addShortcut(item: ItemShortcut) {
        if (item.desktopNumber < 0 || item.cellX < 0 || item.cellY < 0) {
            if (findAreaInCellLayout(item))
                viewModel.onItemCellDataChanged(item)
            return
        }

        val layout = when (item.container) {
            ContainerType.HOT_SEAT -> binding.hotSeat.hotSeatContent
            ContainerType.DESKTOP -> workspace.getChildAt(item.desktopNumber) as CellLayout
        }

        val shortcut = createShortcut(layout, item) ?: return
        if (layout.addViewToCell(shortcut, item)) viewModel.currentItems.add(item)
    }

    private fun findAreaInCellLayout(item: ItemCell): Boolean {
        val position = mutableListOf(-1, -1)
        val desktopNumber = workspace.findEmptyArea(position, item.cellHSpan, item.cellVSpan)

        if (desktopNumber < 0) {
            "Нет свободного места!".showToast(requireContext())
            return false
        }

        item.desktopNumber = desktopNumber
        item.cellX = position[0]
        item.cellY = position[1]

        return true
    }

    private fun createShortcut(parent: ViewGroup, item: ItemShortcut): View? {
        val view = layoutInflater.inflate(R.layout.item_application, parent, false)
                as? AppCompatTextView ?: return null

        view.setOnClickListener { openApp(item.intent) }
        view.setOnLongClickListener { workspace.startDrag(it); return@setOnLongClickListener false }
        view.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null)

        val name = packageManager.getActivityInfo(
            ComponentName(item.packageName, item.className),
            0
        ).loadLabel(packageManager).toString()
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
            "App's not installed!".showToast(requireContext())
        }
    }

    private fun openDeleteAppDialog(item: ItemShortcut) {
        "Delete App".easyLog(this)
        val packageURI = Uri.parse("package:${item.packageName}")
        val intent = Intent(Intent.ACTION_DELETE, packageURI)
        startActivity(intent)
    }

    private fun snapToDesktop(desktopNumber: Int) {
        if (workspace.getCurrentPageNumber() != desktopNumber)
            workspace.snapToPage(desktopNumber)
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
        //isWorkspaceVisible = true
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
//        isWorkspaceVisible = false
    }

//    override fun onBackPressed() {
//        workspace.snapToDefaultPage()
//    }

    /**-------Работа с виджетами-------*/
    companion object {
        private const val REQUEST_CREATE_APPWIDGET = 2
        private const val REQUEST_BIND_APPWIDGET = 3
        private const val REQUEST_PICK_APPWIDGET = 4
    }

    private fun addWidget(widget: ItemWidget) {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()

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
            }

            viewModel.queryToAddWidgets[appWidgetId] = widget
            startActivityForResult(intent, REQUEST_BIND_APPWIDGET)
        }
    }

    private fun createWidget(appWidgetId: Int, widget: ItemWidget? = null) {
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

        if (widget == null) {
            val item = ItemCell(
                type = WorkspaceItemType.WIDGET,
                container = ContainerType.DESKTOP,
                packageName = appWidgetInfo.provider.packageName,
                className = appWidgetInfo.provider.className,
                cellHSpan = -1,
                cellVSpan = -1
            )
            viewModel.saveItem(item)
            return
        }

        if (widget.cellHSpan < 0 || widget.cellVSpan < 0)
            (workspace.getChildAt(0) as CellLayout).calculateItemDimensions(
                widget,
                appWidgetInfo.minHeight,
                appWidgetInfo.minWidth
            )

        if (widget.desktopNumber < 0 || widget.cellX < 0 || widget.cellY < 0) {
            if (findAreaInCellLayout(widget)) {
                viewModel.onItemCellDataChanged(widget)
                snapToDesktop(widget.desktopNumber)
            }
            return
        }

        val layout = workspace.getChildAt(widget.desktopNumber) as? CellLayout ?: return
        val widgetView = appWidgetHost.createView(applicationContext, appWidgetId, appWidgetInfo)
        widgetView.setAppWidget(appWidgetId, appWidgetInfo)
        val params = CellLayoutParams(widget.cellX, widget.cellY, widget.cellHSpan, widget.cellVSpan)
        widgetView.layoutParams = params

        val widgetItem = ItemWidget(widget)
        widgetItem.appWidgetId = appWidgetId
        widgetView.tag = widgetItem

        widgetView.setOnLongClickListener {
            workspace.startDrag(it)
            return@setOnLongClickListener false
        }

        if (layout.addViewToCell(widgetView, -1, 30, params))
            viewModel.currentItems.add(widget)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val appWidgetId = data?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: return
            when(requestCode) {
                REQUEST_BIND_APPWIDGET -> {
                    val widget = viewModel.queryToAddWidgets[appWidgetId] as? ItemWidget ?: return
                    viewModel.queryToAddWidgets.remove(appWidgetId)
                    createWidget(appWidgetId, widget)
                }
                REQUEST_PICK_APPWIDGET -> configureWidget(appWidgetId)
                REQUEST_CREATE_APPWIDGET -> createWidget(appWidgetId)
            }
        }
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

    private fun selectWidget() {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
    }

    private fun deleteWidget(itemWidget: ItemWidget) {
        "WidgetDelete".easyLog(this)
        appWidgetHost.deleteAppWidgetId(itemWidget.appWidgetId)
        viewModel.deleteItem(itemWidget)
    }
    /**-------Конец - Работа с виджетами-------*/
}