package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.AsyncUseCase
import ru.bis.entities.Either
import ru.bis.entities.None

class DeleteCell(private val repository: Repository) : AsyncUseCase<None, DeleteCell.Params, None>() {

    data class Params(val itemCell: ItemCell)

    override suspend fun run(params: Params): Either<None, None> = repository.deleteCell(params.itemCell)
}