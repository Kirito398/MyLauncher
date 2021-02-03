package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType
import ru.bis.entities.AsyncUseCase
import ru.bis.entities.Either
import ru.bis.entities.None

class UpdateShortcut(private val repository: Repository) : AsyncUseCase<None, UpdateShortcut.Params, None>() {
    override suspend fun run(params: Params): Either<None, None>  {
        return when(params.itemShortcut.type) {
            WorkspaceItemType.SHORTCUT -> repository.updateShortcuts(params.itemShortcut)
            WorkspaceItemType.WIDGET -> repository.updateWidget(params.itemShortcut)
        }
    }

    data class Params(val itemShortcut: ItemCell)
}