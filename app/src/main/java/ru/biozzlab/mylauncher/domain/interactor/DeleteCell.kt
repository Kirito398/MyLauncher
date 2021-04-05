package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class DeleteCell(private val repository: Repository) : AsyncUseCase<None, DeleteCell.Params, None>() {

    data class Params(val itemCell: ItemCell)

    override suspend fun run(params: Params): Either<None, None> = repository.deleteCell(params.itemCell)
}