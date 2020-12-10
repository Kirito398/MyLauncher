package ru.biozzlab.mylauncher.domain.interfaces

import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.Either
import ru.bis.entities.None

interface Repository {
    fun loadShortcuts(): Either<None, List<ItemCell>>
    fun updateShortcuts(shortcut: ItemCell): Either<None, None>
    fun isWorkspaceInit(): Either<None, Boolean>
}