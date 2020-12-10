package ru.biozzlab.mylauncher.data

import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.Either
import ru.bis.entities.None

interface Cache {
    fun loadShortcuts(): Either<None, List<ItemCell>>
    fun updateShortcut(shortcut: ItemCell): Either<None, None>
    fun getIsWorkspaceInit(): Either<None, Boolean>
}