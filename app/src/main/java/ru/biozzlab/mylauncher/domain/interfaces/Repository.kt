package ru.biozzlab.mylauncher.domain.interfaces

import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.Either
import ru.bis.entities.None

interface Repository {
    fun loadShortcuts(): Either<None, MutableList<ItemCell>>
    fun loadWidgets(): Either<None, MutableList<ItemCell>>
    fun updateShortcuts(shortcut: ItemCell): Either<None, None>
    fun updateWidget(widget: ItemCell): Either<None, None>
    fun isWorkspaceInit(): Either<None, Boolean>
    fun saveShortcuts(shortcuts: MutableList<ItemCell>): Either<None, MutableList<ItemCell>>
}