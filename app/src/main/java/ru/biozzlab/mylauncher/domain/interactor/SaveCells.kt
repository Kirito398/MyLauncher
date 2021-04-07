package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class SaveCells(val repository: Repository) : AsyncUseCase<List<ItemCell>, SaveCells.Params, None>() {
    override suspend fun run(params: Params): Either<None, List<ItemCell>> = repository.saveCells(params.itemCell)

    data class Params(val itemCell: List<ItemCell>)
}