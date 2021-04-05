package ru.biozzlab.mylauncher.domain.interfaces

import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.sir.core.Either
import ru.sir.core.None

interface Repository {
    fun insertCell(itemCell: ItemCell): Either<None, ItemCell>
    fun updateCell(itemCell: ItemCell): Either<None, None>
    fun loadCells(): Either<None, MutableList<ItemCell>>
    fun isWorkspaceInit(): Either<None, Boolean>
    fun saveCells(shortcuts: MutableList<ItemCell>): Either<None, MutableList<ItemCell>>
    fun deleteCell(itemCell: ItemCell): Either<None, None>
}