package ru.biozzlab.mylauncher.ui.fragments

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
import ru.biozzlab.mylauncher.ui.activity_contracts.AppWidgetBindContract
import ru.biozzlab.mylauncher.ui.activity_contracts.AppWidgetCreateContract
import ru.biozzlab.mylauncher.ui.activity_contracts.AppWidgetPickContract
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
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }

    override fun onBackPressed() {
        workspace.snapToDefaultPage()
    }

    /**-------Работа с виджетами-------*/
    private val requestAppWidgetBind = registerForActivityResult(AppWidgetBindContract()) { appWidgetId ->
        appWidgetId ?: return@registerForActivityResult
        if (appWidgetId < 0) return@registerForActivityResult

        val widget = viewModel.queryToAddWidgets[appWidgetId] as? ItemWidget ?: return@registerForActivityResult
        viewModel.queryToAddWidgets.remove(appWidgetId)
        createWidget(appWidgetId, widget)
    }

    private val requestAppWidgetPick = registerForActivityResult(AppWidgetPickContract()) { appWidgetId ->
        appWidgetId ?: return@registerForActivityResult
        if (appWidgetId < 0) return@registerForActivityResult

        configureWidget(appWidgetId)
    }

    private val requestAppWidgetCreate = registerForActivityResult(AppWidgetCreateContract()) { appWidgetId ->
        appWidgetId ?: return@registerForActivityResult
        if (appWidgetId < 0) return@registerForActivityResult

        createWidget(appWidgetId)
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
            viewModel.queryToAddWidgets[appWidgetId] = widget
            requestAppWidgetBind.launch(appWidgetId to widgetProvider)
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

    private fun configureWidget(appWidgetId: Int) {
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

        if (appWidgetInfo.configure != null) {
            requestAppWidgetCreate.launch(appWidgetInfo.configure to appWidgetId)
        } else {
            createWidget(appWidgetId)
        }
    }

    private fun selectWidget() {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        requestAppWidgetPick.launch(appWidgetId)
    }

    private fun deleteWidget(itemWidget: ItemWidget) {
        "WidgetDelete".easyLog(this)
        appWidgetHost.deleteAppWidgetId(itemWidget.appWidgetId)
        viewModel.deleteItem(itemWidget)
    }
    /**-------Конец - Работа с виджетами-------*/
}