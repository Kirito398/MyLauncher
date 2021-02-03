package ru.biozzlab.mylauncher.data

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.Either
import ru.bis.entities.None

class RepositoryImpl(private val cache: Cache) : Repository {
    override fun loadShortcuts(): Either<None, MutableList<ItemCell>> = cache.loadShortcuts()
    override fun loadWidgets(): Either<None, MutableList<ItemCell>> = cache.loadWidgets()
    override fun updateShortcuts(shortcut: ItemCell): Either<None, None> = cache.updateShortcut(shortcut)
    override fun updateWidget(widget: ItemCell): Either<None, None> = cache.updateWidget(widget)
    override fun isWorkspaceInit(): Either<None, Boolean> = cache.getIsWorkspaceInit()
    override fun saveShortcuts(shortcuts: MutableList<ItemCell>): Either<None, MutableList<ItemCell>> = cache.saveShortcuts(shortcuts)
}