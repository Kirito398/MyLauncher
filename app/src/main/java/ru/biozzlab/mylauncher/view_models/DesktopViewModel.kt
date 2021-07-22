package ru.biozzlab.mylauncher.view_models

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import ru.biozzlab.mylauncher.domain.interactor.DeleteCell
import ru.biozzlab.mylauncher.domain.interactor.SaveCells
import ru.biozzlab.mylauncher.domain.interactor.UpdateCells
import ru.biozzlab.mylauncher.domain.interactor.WorkSpaceItems
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType
import ru.biozzlab.mylauncher.easyLog
import ru.biozzlab.mylauncher.ui.receivers.PackageStatusChangeReceiver
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import java.lang.Exception
import javax.inject.Inject

class DesktopViewModel @Inject constructor(
    application: Application,
    loadWorkspaceItems: WorkSpaceItems,
    private val updateCell: UpdateCells,
    private val saveCells: SaveCells,
    private val deleteCell: DeleteCell
) : BaseViewModel(application) {

    private var isWorkspaceInit = false

    private val appsReceiver: PackageStatusChangeReceiver = PackageStatusChangeReceiver()

    private val _removedItems = MutableStateFlow(listOf<ItemCell>())
    val removedItems = _removedItems.asStateFlow()

    private val queryToUpdate = mutableListOf<ItemCell>()
    val queryToAddWidgets = mutableMapOf<Int, ItemCell>()
    val currentItems = mutableListOf<ItemCell>()

    val workspaceItems: StateFlow<List<ItemCell>> = loadWorkspaceItems(None())
        .map { checkIfItemRemoved(it) }
        .map { removeDuplicateItems(it, currentItems) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        appsReceiver.setOnInstallPackageListener { packageName ->
            if (currentItems.find { it.packageName == packageName } != null) return@setOnInstallPackageListener
            createItemCellFromPackageName(packageName)?.let { saveItem(it) }
        }
        appsReceiver.setOnDeletePackageListener { packageName ->
            val item = currentItems.find { it.packageName == packageName } ?: return@setOnDeletePackageListener
            deleteItem(item)
        }
    }

    override fun onResume() {
        val filters = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addDataScheme("package")
        }
        context.registerReceiver(appsReceiver, filters)
        //if (isWorkspaceInit) updateDesktop()
    }

    override fun onStop() {
        context.unregisterReceiver(appsReceiver)
    }

    fun onItemCellDataChanged(item: ItemCell) {
        queryToUpdate.add(item)
        updateItem()
    }

    fun updateDesktop() {
        updateItem()
        findNewItems()
        isWorkspaceInit = true
    }

    fun saveItem(item: ItemCell) {
        saveItems(listOf(item))
    }

    fun deleteItem(item: ItemCell) {
        deleteCell(DeleteCell.Params(item))
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

    private fun removeDuplicateApps(fromList: MutableList<ApplicationInfo>, defaultAppList: List<ItemCell>): MutableList<ItemCell> {
        val list = mutableListOf<ItemCell>()
        fromList.forEach { info ->
            val isNew = defaultAppList.find { it.packageName == info.packageName } == null
            if (isNew) convertApplicationInfoToItemCell(info)?.let { list.add(it) }
        }
        return list
    }

    private fun checkIfItemRemoved(items: List<ItemCell>): List<ItemCell> {
        val removed = removeDuplicateItems(currentItems, items)
        currentItems.removeAll(removed)
        _removedItems.value = removed
        return items
    }

    private fun removeDuplicateItems(from: List<ItemCell>, source: List<ItemCell>): List<ItemCell> {
        val list = mutableListOf<ItemCell>()
        from.forEach { item ->
            if (source.find { it.packageName == item.packageName } == null
                || (item.type == WorkspaceItemType.WIDGET && source.find { it.id == item.id } == null))
                list.add(item)
        }
        return list
    }

    private fun createItemCellFromPackageName(packageName: String): ItemCell? {
        val className = context.packageManager.getLaunchIntentForPackage(packageName)?.component?.className ?: return null
        return ItemCell(
            type = WorkspaceItemType.SHORTCUT,
            container = ContainerType.DESKTOP,
            packageName = packageName,
            className = className
        )
    }

    private fun convertApplicationInfoToItemCell(info: ApplicationInfo): ItemCell? = createItemCellFromPackageName(info.packageName)

    private fun checkForLaunchIntent(appList: List<ApplicationInfo>): MutableList<ApplicationInfo> {
        val launchApps = mutableListOf<ApplicationInfo>()

        for (info in appList)
            if (checkForLaunchIntent(info.packageName)) launchApps.add(info)

        return launchApps
    }

    fun checkForLaunchIntent(packageName: String): Boolean {
        val packageManager = context.packageManager
        return try {
            packageManager.getLaunchIntentForPackage(packageName) != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}