package ru.biozzlab.mylauncher.data

import kotlinx.coroutines.flow.Flow
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.sir.core.Either
import ru.sir.core.None

interface Cache {
    fun insertCell(itemCell: ItemCell): Either<None, ItemCell>
    fun loadCells(): Either<None, MutableList<ItemCell>>
    fun updateCell(itemCell: ItemCell): Either<None, ItemCell>
    fun updateCells(cells: List<ItemCell>): Either<None, List<ItemCell>>
    fun getIsWorkspaceInit(): Either<None, Boolean>
    fun saveCells(cells: List<ItemCell>): Either<None, List<ItemCell>>
    fun deleteCell(itemCell: ItemCell): Either<None, None>

    fun workspaceCells(): Flow<MutableList<ItemCell>>
}