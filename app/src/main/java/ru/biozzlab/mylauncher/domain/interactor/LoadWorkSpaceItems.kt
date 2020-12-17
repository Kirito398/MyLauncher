package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.AsyncUseCase
import ru.bis.entities.Either
import ru.bis.entities.None

class LoadWorkSpaceItems(private val repository: Repository) : AsyncUseCase<MutableList<ItemCell>, None, None>() {
    override suspend fun run(params: None): Either<None, MutableList<ItemCell>> {
        val shortcuts = repository.loadShortcuts()
        val widgets = repository.loadWidgets()
        val items = mutableListOf<ItemCell>()

        if (shortcuts is Either.Right<MutableList<ItemCell>>)
            items.addAll(shortcuts.r)
        else return Either.Left(None())

        if (widgets is Either.Right<MutableList<ItemCell>>)
            items.addAll(widgets.r)
        else return Either.Left(None())

        return Either.Right(items)
    }
}