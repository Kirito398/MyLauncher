package ru.biozzlab.mylauncher.domain.interfaces

import kotlinx.coroutines.flow.Flow
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.sir.core.Either
import ru.sir.core.None

interface Repository {
    fun insertCell(itemCell: ItemCell): Either<None, ItemCell>
    fun updateCell(itemCell: ItemCell): Either<None, ItemCell>
    fun updateCells(cells: List<ItemCell>): Either<None, List<ItemCell>>
    fun loadCells(): Either<None, MutableList<ItemCell>>
    fun isWorkspaceInit(): Either<None, Boolean>
    fun saveCells(shortcuts: List<ItemCell>): Either<None, List<ItemCell>>
    fun deleteCell(itemCell: ItemCell): Either<None, None>

    fun cells(): Flow<MutableList<ItemCell>>
}