package ru.biozzlab.mylauncher.cache

import ru.biozzlab.mylauncher.cache.entities.CellEntity
import ru.biozzlab.mylauncher.cache.entities.WidgetEntity
import ru.biozzlab.mylauncher.data.Cache
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType
import ru.bis.entities.Either
import ru.bis.entities.None

class CacheImpl(private val roomManager: RoomManager, private val prefsManager: SharedPrefsManager) : Cache {
    override fun loadShortcuts(): Either<None, MutableList<ItemCell>>
            = convertCellEntitiesToModel(roomManager.cellDao().getAllCells())

    override fun loadWidgets(): Either<None, MutableList<ItemCell>>
        = convertWidgetEntitiesToModel(roomManager.widgetDao().getAllWidgets())

    override fun updateShortcut(shortcut: ItemCell): Either<None, None> {
        roomManager.cellDao().update(convertModelToCellEntities(shortcut))
        return Either.Right(None())
    }

    override fun getIsWorkspaceInit(): Either<None, Boolean> = prefsManager.getIsWorkspaceInit()

    override fun saveShortcuts(shortcuts: MutableList<ItemCell>): Either<None, MutableList<ItemCell>> {
        for (shortcut in shortcuts) roomManager.cellDao().insert(convertModelToCellEntities(shortcut))
        prefsManager.setIsWorkspaceInit()
        return loadShortcuts()
    }

    private fun convertWidgetEntitiesToModel(entities: List<WidgetEntity>): Either<None, MutableList<ItemCell>> {
        val list = mutableListOf<ItemCell>()
        for (entity in entities)
            list.add(
                ItemCell(
                    entity.id,
                    WorkspaceItemType.WIDGET,
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

    private fun convertCellEntitiesToModel(entities: List<CellEntity>): Either<None, MutableList<ItemCell>> {
        val list = mutableListOf<ItemCell>()
        for (entity in entities)
            list.add(
                ItemCell(
                    entity.id,
                    WorkspaceItemType.SHORTCUT,
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

    private fun convertModelToCellEntities(model: ItemCell): CellEntity {
        val entity = CellEntity(
            model.packageName,
            model.className,
            model.container.id,
            model.cellX,
            model.cellY,
            model.desktopNumber
        )

        if (model.id >= 0) entity.id = model.id

        return entity
    }
}