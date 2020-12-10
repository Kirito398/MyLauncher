package ru.biozzlab.mylauncher.cache

import ru.biozzlab.mylauncher.cache.entities.CellEntity
import ru.biozzlab.mylauncher.data.Cache
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.bis.entities.Either
import ru.bis.entities.None

class CacheImpl(private val roomManager: RoomManager, private val prefsManager: SharedPrefsManager) : Cache {
    override fun loadShortcuts(): Either<None, List<ItemCell>>
            = convertEntitiesToModel(roomManager.cellDao().getAllCells())

    override fun updateShortcut(shortcut: ItemCell): Either<None, None> {
        roomManager.cellDao().update(convertModelToEntities(shortcut))
        return Either.Right(None())
    }

    override fun getIsWorkspaceInit(): Either<None, Boolean> = prefsManager.getIsWorkspaceInit()

    private fun convertEntitiesToModel(entities: List<CellEntity>): Either<None, List<ItemCell>> {
        val list = mutableListOf<ItemCell>()
        for (entity in entities)
            list.add(
                ItemCell(
                    entity.id,
                ContainerType.fromID(entity.container) ?: ContainerType.DESKTOP,
                    entity.packageName,
                    entity.className,
                    entity.cellX,
                    entity.cellY,
                    entity.desktopNumber
                )
            )
        return Either.Right(list)
    }

    private fun convertModelToEntities(model: ItemCell): CellEntity {
        val entity = CellEntity(
            model.packageName,
            model.className,
            model.container.id,
            model.cellX,
            model.cellY,
            model.desktopNumber
        )

        if (model.id > 0) entity.id = model.id

        return entity
    }
}