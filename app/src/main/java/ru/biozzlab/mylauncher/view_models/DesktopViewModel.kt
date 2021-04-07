package ru.biozzlab.mylauncher.view_models

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import ru.biozzlab.mylauncher.domain.interactor.SaveCells
import ru.biozzlab.mylauncher.domain.interactor.UpdateCells
import ru.biozzlab.mylauncher.domain.interactor.WorkSpaceItems
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType
import ru.biozzlab.mylauncher.easyLog
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import java.lang.Exception
import javax.inject.Inject

class DesktopViewModel @Inject constructor(
    application: Application,
    loadWorkspaceItems: WorkSpaceItems,
    private val updateCell: UpdateCells,
    private val saveCells: SaveCells
) : BaseViewModel(application) {

    private val queryToUpdate = mutableListOf<ItemCell>()
    val queryToAddWidgets = mutableMapOf<Int, ItemCell>()
    val currentItems = mutableListOf<ItemCell>()

    val workspaceItems: StateFlow<List<ItemCell>> = loadWorkspaceItems(None())
        .map { removeDuplicateItems(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onItemCellDataChanged(item: ItemCell) {
        queryToUpdate.add(item)
        updateItem()
    }

    fun updateDesktop() {
        updateItem()
        findNewItems()
    }

    fun saveItem(item: ItemCell) {
        saveItems(listOf(item))
    }

    private fun saveItems(items: List<ItemCell>) {
        saveCells(SaveCells.Params(items))
    }

    private fun updateItem() {
        if (queryToUpdate.isEmpty()) return
        updateCell(UpdateCells.Params(queryToUpdate)) {
            it.either({}, { items ->
                queryToUpdate.removeAll(items)
                updateItem()
            })
        }
    }

    private fun findNewItems() {
        val installedApps = getInstalledApplications()
        val items = mutableListOf<ItemCell>()
        items.addAll(currentItems)
        items.addAll(queryToUpdate)
        saveItems(removeDuplicateApps(installedApps, items))
        "Find new package!".easyLog(this)
    }

    private fun getInstalledApplications() =
        checkForLaunchIntent(context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA))

    private fun removeDuplicateItems(items: List<ItemCell>): List<ItemCell> {
        val list = mutableListOf<ItemCell>()
        items.forEach { item ->
            if (currentItems.find { it.packageName == item.packageName } == null
                || (item.type == WorkspaceItemType.WIDGET && currentItems.find { it.id == item.id } == null))
                    list.add(item)
        }
        return list
    }

    private fun removeDuplicateApps(fromList: MutableList<ApplicationInfo>, defaultAppList: List<ItemCell>): MutableList<ItemCell> {
        val list = mutableListOf<ItemCell>()
        fromList.forEach { info ->
            val isNew = defaultAppList.find { it.packageName == info.packageName } == null
            if (isNew) list.add(convertApplicationInfoToItemCell(info))
        }
        return list
    }

    private fun convertApplicationInfoToItemCell(info: ApplicationInfo): ItemCell {
        return ItemCell(
            type = WorkspaceItemType.SHORTCUT,
            container = ContainerType.DESKTOP,
            packageName = info.packageName,
            className = context.packageManager.getLaunchIntentForPackage(info.packageName)?.component?.className ?: ""
        )
    }

    private fun checkForLaunchIntent(appList: List<ApplicationInfo>): MutableList<ApplicationInfo> {
        val launchApps = mutableListOf<ApplicationInfo>()
        val packageManager = context.packageManager

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
}