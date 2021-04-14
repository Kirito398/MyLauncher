package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class UpdateCells(private val repository: Repository) : AsyncUseCase<List<ItemCell>, UpdateCells.Params, None>() {
    override suspend fun run(params: Params): Either<None, List<ItemCell>> = repository.updateCells(params.cells)

    data class Params(val cells: List<ItemCell>)
}