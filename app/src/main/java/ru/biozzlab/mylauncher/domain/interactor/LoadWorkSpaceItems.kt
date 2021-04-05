package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class LoadWorkSpaceItems(private val repository: Repository) : AsyncUseCase<MutableList<ItemCell>, None, None>() {
    override suspend fun run(params: None): Either<None, MutableList<ItemCell>> = repository.loadCells()
}