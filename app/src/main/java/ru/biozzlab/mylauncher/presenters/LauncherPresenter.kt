package ru.biozzlab.mylauncher.presenters

import ru.biozzlab.mylauncher.domain.interactor.LoadCells
import ru.biozzlab.mylauncher.domain.interactor.UpdateShortcut
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.bis.entities.None

class LauncherPresenter(
    private val loadCells: LoadCells,
    private val updateShortcut: UpdateShortcut) : LauncherViewContract.Presenter {
    private lateinit var view: LauncherViewContract.View

    override fun setView(view: LauncherViewContract.View) {
        this.view = view
    }

    override fun init() {
        view.checkForLocaleChanged()
        view.setContentView()
        view.initViews()
        view.setListeners()

        loadCells(None()) { it.either(::onCellsLoadFailed, ::onCellsLoaded) }
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