package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class UpdateCell(private val repository: Repository) : AsyncUseCase<ItemCell, UpdateCell.Params, None>() {
    override suspend fun run(params: Params): Either<None, ItemCell> = repository.updateCell(params.itemCell)

    data class Params(val itemCell: ItemCell)
}