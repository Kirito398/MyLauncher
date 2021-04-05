package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class InsertCell(private val repository: Repository) : AsyncUseCase<ItemCell, InsertCell.Params, None>() {

    override suspend fun run(params: Params): Either<None, ItemCell> = repository.insertCell(params.itemCell)

    data class Params(val itemCell: ItemCell)
}