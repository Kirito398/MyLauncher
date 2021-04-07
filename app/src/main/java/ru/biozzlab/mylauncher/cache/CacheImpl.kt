package ru.biozzlab.mylauncher.cache

import kotlinx.coroutines.flow.*
import ru.biozzlab.mylauncher.cache.entities.CellEntity
import ru.biozzlab.mylauncher.data.Cache
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType
import ru.sir.core.Either
import ru.sir.core.None

class CacheImpl(private val roomManager: RoomManager, private val prefsManager: SharedPrefsManager) : Cache {
    override fun insertCell(itemCell: ItemCell): Either<None, ItemCell> {
        roomManager.cellDao().insert(convertModelToCellEntities(itemCell))
        return Either.Right(itemCell)
    }

    override fun loadCells(): Either<None, MutableList<ItemCell>> =
        Either.Right(convertCellEntitiesToModel(roomManager.cellDao().getAllCells()))

    override fun workspaceCells(): Flow<MutableList<ItemCell>> =
        roomManager.cellDao().getAllCellsFlow()
            .map { convertCellEntitiesToModel(it) }

    override fun updateCell(itemCell: ItemCell): Either<None, ItemCell> {
        roomManager.cellDao().update(convertModelToCellEntities(itemCell))
        return Either.Right(itemCell)
    }

    override fun updateCells(cells: List<ItemCell>): Either<None, List<ItemCell>> {
        val item = roomManager.cellDao().updateList(convertModelToCellEntities(cells))
        return Either.Right(cells)
    }

    override fun getIsWorkspaceInit(): Either<None, Boolean> = prefsManager.getIsWorkspaceInit()

    override fun saveCells(cells: List<ItemCell>): Either<None, List<ItemCell>> {
        roomManager.cellDao().insertList(convertModelToCellEntities(cells))
        prefsManager.setIsWorkspaceInit()
        return loadCells()
    }

    override fun deleteCell(itemCell: ItemCell): Either<None, None> {
        roomManager.cellDao().delete(convertModelToCellEntities(itemCell))
        return Either.Right(None())
    }

    private fun convertCellEntitiesToModel(entities: List<CellEntity>): MutableList<ItemCell> {
        val list = mutableListOf<ItemCell>()
        for (entity in entities) list.add(convertCellEntitiesToModel(entity))
        return list
    }

    private fun convertCellEntitiesToModel(entity: CellEntity): ItemCell {
        return ItemCell(
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
    }

    private fun convertModelToCellEntities(models: List<ItemCell>): MutableList<CellEntity> {
        val list = mutableListOf<CellEntity>()
        for (model in models) list.add(convertModelToCellEntities(model))
        return list
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