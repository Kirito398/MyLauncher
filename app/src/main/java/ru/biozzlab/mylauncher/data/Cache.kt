package ru.biozzlab.mylauncher.data

import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.Either
import ru.bis.entities.None

interface Cache {
    fun insertCell(itemCell: ItemCell): Either<None, ItemCell>
    fun loadCells(): Either<None, MutableList<ItemCell>>
    fun updateCell(itemCell: ItemCell): Either<None, None>
    fun getIsWorkspaceInit(): Either<None, Boolean>
    fun saveCells(cells: MutableList<ItemCell>): Either<None, MutableList<ItemCell>>
}