package ru.biozzlab.mylauncher.data

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.sir.core.Either
import ru.sir.core.None

class RepositoryImpl(private val cache: Cache) : Repository {
    override fun insertCell(itemCell: ItemCell): Either<None, ItemCell> = cache.insertCell(itemCell)
    override fun updateCell(itemCell: ItemCell): Either<None, None> = cache.updateCell(itemCell)
    override fun loadCells(): Either<None, MutableList<ItemCell>> = cache.loadCells()
    override fun isWorkspaceInit(): Either<None, Boolean> = cache.getIsWorkspaceInit()
    override fun saveCells(cells: MutableList<ItemCell>): Either<None, MutableList<ItemCell>> = cache.saveCells(cells)
    override fun deleteCell(itemCell: ItemCell): Either<None, None> = cache.deleteCell(itemCell)
}