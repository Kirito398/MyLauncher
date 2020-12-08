package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.bis.entities.AsyncUseCase
import ru.bis.entities.Either
import ru.bis.entities.None

class UpdateShortcut(private val repository: Repository) : AsyncUseCase<None, UpdateShortcut.Params, None>() {
    override suspend fun run(params: Params): Either<None, None> = repository.updateShortcuts(params.itemShortcut)

    data class Params(val itemShortcut: ItemShortcut)
}