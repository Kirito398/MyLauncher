package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.AsyncUseCase
import ru.bis.entities.Either
import ru.bis.entities.None

class LoadWorkSpaceItems(private val repository: Repository) : AsyncUseCase<MutableList<ItemCell>, None, None>() {
    override suspend fun run(params: None): Either<None, MutableList<ItemCell>> = repository.loadCells()
}