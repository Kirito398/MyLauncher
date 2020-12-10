package ru.biozzlab.mylauncher.presenters

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import ru.biozzlab.mylauncher.domain.interactor.IsWorkspaceInit
import ru.biozzlab.mylauncher.domain.interactor.LoadCells
import ru.biozzlab.mylauncher.domain.interactor.UpdateShortcut
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.bis.entities.Either
import ru.bis.entities.None
import java.lang.Exception

class LauncherPresenter(
    private val loadCells: LoadCells,
    private val updateShortcut: UpdateShortcut,
    private val isWorkspaceInit: IsWorkspaceInit) : LauncherViewContract.Presenter {
    private lateinit var view: LauncherViewContract.View

    override fun setView(view: LauncherViewContract.View) {
        this.view = view
    }

    override fun init() {
        view.checkForLocaleChanged()
        view.setContentView()
        view.initViews()
        view.setListeners()

        isWorkspaceInit(None()) { if (it is Either.Right && it.r) initWorkspace() }

        loadCells(None()) { it.either(::onCellsLoadFailed, ::onCellsLoaded) }
    }

    private fun initWorkspace() {
        loadCells(None()) { it.either({}, ::onLoadedDefaultApp) }
    }

    private fun onLoadedDefaultApp(defaultAppList: List<ItemCell>) {
        val packageManager = view.getPackageManager()

        val allAppList = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA))
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

    override fun onItemShortcutDataChanged(item: ItemShortcut) {
        updateShortcut(UpdateShortcut.Params(item))
    }

    private fun onCellsLoaded(cells: List<ItemCell>) {
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