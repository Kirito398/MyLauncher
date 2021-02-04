package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.AsyncUseCase
import ru.bis.entities.Either
import ru.bis.entities.None

class InsertCell(private val repository: Repository) : AsyncUseCase<ItemCell, InsertCell.Params, None>() {

    override suspend fun run(params: Params): Either<None, ItemCell> = repository.insertCell(params.itemCell)

    data class Params(val itemCell: ItemCell)
}