package ru.biozzlab.mylauncher.cache

import ru.biozzlab.mylauncher.cache.entities.CellEntity
import ru.biozzlab.mylauncher.data.Cache
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.bis.entities.Either
import ru.bis.entities.None

class CacheImpl(private val roomManager: RoomManager) : Cache {
    override fun loadShortcuts(): Either<None, List<ItemCell>>
            = convertEntitiesToModel(roomManager.cellDao().getAllCells())

    private fun convertEntitiesToModel(entities: List<CellEntity>): Either<None, List<ItemCell>> {
        val list = mutableListOf<ItemCell>()
        for (entity in entities)
            list.add(
                ItemCell(
                ContainerType.fromID(entity.container) ?: ContainerType.DESKTOP,
                    entity.packageName,
                    entity.className,
                    entity.cellX,
                    entity.cellY
                )
            )
        return Either.Right(list)
    }
}