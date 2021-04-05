package ru.biozzlab.mylauncher.cache

import ru.biozzlab.mylauncher.cache.entities.CellEntity
import ru.biozzlab.mylauncher.data.Cache
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType
import ru.bis.entities.Either
import ru.bis.entities.None

class CacheImpl(private val roomManager: RoomManager, private val prefsManager: SharedPrefsManager) : Cache {
    override fun insertCell(itemCell: ItemCell): Either<None, ItemCell> {
        roomManager.cellDao().insert(convertModelToCellEntities(itemCell))
        return Either.Right(itemCell)
    }

    override fun loadCells(): Either<None, MutableList<ItemCell>> =
        convertCellEntitiesToModel(roomManager.cellDao().getAllCells())

    override fun updateCell(itemCell: ItemCell): Either<None, None> {
        roomManager.cellDao().update(convertModelToCellEntities(itemCell))
        return Either.Right(None())
    }

    override fun getIsWorkspaceInit(): Either<None, Boolean> = prefsManager.getIsWorkspaceInit()

    override fun saveCells(cells: MutableList<ItemCell>): Either<None, MutableList<ItemCell>> {
        for (shortcut in cells) roomManager.cellDao().insert(convertModelToCellEntities(shortcut))
        prefsManager.setIsWorkspaceInit()
        return loadCells()
    }

    override fun deleteCell(itemCell: ItemCell): Either<None, None> {
        roomManager.cellDao().delete(convertModelToCellEntities(itemCell))
        return Either.Right(None())
    }

    private fun convertCellEntitiesToModel(entities: List<CellEntity>): Either<None, MutableList<ItemCell>> {
        val list = mutableListOf<ItemCell>()
        for (entity in entities)
            list.add(
                ItemCell(
                    entity.id,
                    WorkspaceItemType.fromID(entity.type) ?: WorkspaceItemType.SHORTCUT,
                ContainerType.fromID(entity.container) ?: ContainerType.DESKTOP,
                    entity.packageName,
                    entity.className,
                    entity.cellX,
                    entity.cellY,
                    entity.desktopNumber,
                    entity.spanX,
                    entity.spanY
                )
            )
        return Either.Right(list)
    }

    private fun convertModelToCellEntities(model: ItemCell): CellEntity {
        val entity = CellEntity(
            model.packageName,
            model.className,
            model.container.id,
            model.cellX,
            model.cellY,
            model.desktopNumber,
            model.type.type,
            model.cellHSpan,
            model.cellVSpan
        )

        if (model.id >= 0) entity.id = model.id

        return entity
    }
}