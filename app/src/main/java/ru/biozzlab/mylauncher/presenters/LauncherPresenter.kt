package ru.biozzlab.mylauncher.presenters

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import ru.biozzlab.mylauncher.copy
import ru.biozzlab.mylauncher.domain.interactor.*
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.biozzlab.mylauncher.domain.models.ItemWidget
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType
import ru.biozzlab.mylauncher.easyLog
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.bis.entities.Either
import ru.bis.entities.None
import java.lang.Exception

class LauncherPresenter(
    private val loadWorkspaceItems: LoadWorkSpaceItems,
    private val updateCell: UpdateCell,
    private val isWorkspaceInit: IsWorkspaceInit,
    private val saveCells: SaveCells,
    private val insertCell: InsertCell,
    private val deleteCell: DeleteCell) : LauncherViewContract.Presenter {

    private lateinit var view: LauncherViewContract.View
    private val shortcutsTempList = mutableListOf<ItemCell>()
    private val loadedCellsList = mutableListOf<ItemCell>()

    override fun setView(view: LauncherViewContract.View) {
        this.view = view
    }

    override fun init() {
        view.checkForLocaleChanged()
        view.setContentView()
        view.initViews()
        view.setListeners()

        isWorkspaceInit(None()) { if (it is Either.Right) initWorkspace(!it.r) }

        //loadCells(None()) { it.either(::onCellsLoadFailed, ::onCellsLoaded) }
    }

    private fun initWorkspace(isFirstRun: Boolean) {
        view.setWorkspaceInitProgressBarVisibility(true)

        loadWorkspaceItems(None()) {
            it.either({}, { appList ->
                if (isFirstRun)
                    onStartWorkspaceInit(appList)
                else {
                    onCellsLoaded(appList)
                    findNewPackages()
                }

                view.setWorkspaceInitProgressBarVisibility(false)
            })
        }
    }

    private fun onStartWorkspaceInit(defaultAppList: MutableList<ItemCell>) {
        val allAppList = getInstalledApplications()
        val newAppList = removeDuplicateApp(allAppList, defaultAppList)

        defaultAppList.addAll(newAppList)

        onCellsLoaded(defaultAppList)
        onInitWorkspaceFinished()
    }

    private fun onInitWorkspaceFinished() {
        saveShortcutsFromTempList()
    }

    private fun checkForLaunchIntent(appList: List<ApplicationInfo>): MutableList<ApplicationInfo> {
        val launchApps = mutableListOf<ApplicationInfo>()
        val packageManager = view.getPackageManager()

        for (info in appList) {
            try {
                if (packageManager.getLaunchIntentForPackage(info.packageName) != null)
                    launchApps.add(info)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return launchApps
    }

    private fun removeDuplicateApp(fromList: MutableList<ApplicationInfo>, defaultAppList: List<ItemCell>): MutableList<ItemCell> {
        val newAppList = mutableListOf<ItemCell>()

        for (app in fromList) {
            var alreadyAdded = false
            for (defaultApp in defaultAppList) {
                if (app.packageName == defaultApp.packageName)
                    alreadyAdded = true
            }

            if (!alreadyAdded) newAppList.add(convertApplicationInfoToItemCell(app))
        }

        return newAppList
    }

    private fun convertApplicationInfoToItemCell(info: ApplicationInfo): ItemCell {
        val packageManager = view.getPackageManager()

        return ItemCell(
            -1,
            WorkspaceItemType.SHORTCUT,
            ContainerType.DESKTOP,
            info.packageName,
            packageManager.getLaunchIntentForPackage(info.packageName)?.component?.className ?: "",
            -1,
            -1,
            -1,
            1,
            1
        )
    }

    override fun onItemCellDataChanged(item: ItemCell) {
        updateCell(UpdateCell.Params(item))
    }

    override fun addShortcutToUpdateQueue(item: ItemCell) {
        shortcutsTempList.add(item)
    }

    override fun saveItem(item: ItemCell) {
        insertCell(InsertCell.Params(item))
    }

    override fun deleteItem(item: ItemCell) {
        deleteCell(DeleteCell.Params(item))
    }

    override fun saveShortcutsFromTempList() {
        saveCells(SaveCells.Params(shortcutsTempList.copy()))
        shortcutsTempList.clear()
    }

    override fun findNewPackages() {
        val installedApps = getInstalledApplications()
        if (installedApps.size <= loadedCellsList.size) return
        onCellsLoaded(removeDuplicateApp(installedApps, loadedCellsList), false)
        saveShortcutsFromTempList()
        "Find new package!".easyLog(this)
    }

    private fun getInstalledApplications() = checkForLaunchIntent(view.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA))

    private fun onCellsLoaded(cells: MutableList<ItemCell>, clearLoadedList: Boolean = true) {
        if (clearLoadedList) loadedCellsList.clear()

        for (cell in cells) {
            when (cell.type) {
                WorkspaceItemType.SHORTCUT -> {
                    view.addShortcut(ItemShortcut(cell))
                    loadedCellsList.add(cell)
                }
                WorkspaceItemType.WIDGET -> view.addWidget(ItemWidget(cell))
            }
        }
    }

    private fun onCellsLoadFailed(none: None) {
        //TODO
    }
}