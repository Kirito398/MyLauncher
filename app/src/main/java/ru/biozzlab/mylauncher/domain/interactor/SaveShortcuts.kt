package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.AsyncUseCase
import ru.bis.entities.Either
import ru.bis.entities.None

class SaveShortcuts(val repository: Repository) : AsyncUseCase<MutableList<ItemCell>, SaveShortcuts.Params, None>() {
    override suspend fun run(params: Params): Either<None, MutableList<ItemCell>> = repository.saveShortcuts(params.shortcuts)

    data class Params(val shortcuts: MutableList<ItemCell>)
}