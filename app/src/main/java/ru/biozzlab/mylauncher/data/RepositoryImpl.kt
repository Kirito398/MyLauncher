package ru.biozzlab.mylauncher.data

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.bis.entities.Either
import ru.bis.entities.None

class RepositoryImpl(private val cache: Cache) : Repository {
    override fun loadShortcuts(): Either<None, List<ItemCell>> = cache.loadShortcuts()
}