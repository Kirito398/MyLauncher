package ru.biozzlab.mylauncher.presenters

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import ru.biozzlab.mylauncher.domain.interactor.IsWorkspaceInit
import ru.biozzlab.mylauncher.domain.interactor.LoadCells
import ru.biozzlab.mylauncher.domain.interactor.SaveShortcuts
import ru.biozzlab.mylauncher.domain.interactor.UpdateShortcut
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.bis.entities.Either
import ru.bis.entities.None
import java.lang.Exception

class LauncherPresenter(
    private val loadCells: LoadCells,
    private val updateShortcut: UpdateShortcut,
    private val isWorkspaceInit: IsWorkspaceInit,
    private val saveShortcuts: SaveShortcuts) : LauncherViewContract.Presenter {
    private lateinit var view: LauncherViewContract.View
    private val shortcutsTempList = mutableListOf<ItemCell>()

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

        loadCells(None()) {
            it.either({}, { appList ->
                if (isFirstRun)
                    onStartWorkspaceInit(appList)
                else
                    onCellsLoaded(appList)

                view.setWorkspaceInitProgressBarVisibility(false)
            })
        }
    }

    private fun onStartWorkspaceInit(defaultAppList: MutableList<ItemCell>) {
        val packageManager = view.getPackageManager()
        val allAppList = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA))
        val newAppList = removeDuplicateApp(allAppList, defaultAppList)

        defaultAppList.addAll(newAppList)

        onCellsLoaded(defaultAppList)
        onInitWorkspaceFinished()
    }

    private fun onInitWorkspaceFinished() {
        saveShortcuts(SaveShortcuts.Params(shortcutsTempList))
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

    override fun onItemShortcutDataChanged(item: ItemShortcut) {
        updateShortcut(UpdateShortcut.Params(item))
    }

    override fun addShortcutToUpdateQueue(item: ItemShortcut) {
        shortcutsTempList.add(item)
    }

    private fun onCellsLoaded(cells: MutableList<ItemCell>) {
        val shortcuts = convertCellsToShortcuts(cells)
        for (shortcut in shortcuts)
            view.addShortcut(shortcut)
    }

    private fun convertCellsToShortcuts(cells: List<ItemCell>): List<ItemShortcut> {
        val list = mutableListOf<ItemShortcut>()
        for (cell in cells)
            list.add(ItemShortcut(cell))
        return list
    }

    private fun onCellsLoadFailed(none: None) {
        //TODO
    }
}