package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.AsyncUseCase
import ru.bis.entities.Either
import ru.bis.entities.None

class SaveCells(val repository: Repository) : AsyncUseCase<MutableList<ItemCell>, SaveCells.Params, None>() {
    override suspend fun run(params: Params): Either<None, MutableList<ItemCell>> = repository.saveCells(params.itemCell)

    data class Params(val itemCell: MutableList<ItemCell>)
}