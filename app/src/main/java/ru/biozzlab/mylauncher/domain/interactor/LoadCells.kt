package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.AsyncUseCase
import ru.bis.entities.Either
import ru.bis.entities.None

class LoadCells(private val repository: Repository) : AsyncUseCase<List<ItemCell>, None, None>() {
    override suspend fun run(params: None): Either<None, List<ItemCell>> = repository.loadShortcuts()
}