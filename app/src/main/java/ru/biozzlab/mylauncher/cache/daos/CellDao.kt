package ru.biozzlab.mylauncher.cache.daos

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.biozzlab.mylauncher.cache.RoomConstants
import ru.biozzlab.mylauncher.cache.entities.CellEntity
import ru.biozzlab.mylauncher.domain.models.ItemCell

@Dao
interface CellDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cell: CellEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(list: List<CellEntity>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(cell: CellEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateList(cells: List<CellEntity>)

    @Delete
    fun delete(cell: CellEntity)

    @Query("SELECT * FROM ${RoomConstants.TABLE_CELLS}")
    fun getAllCells(): List<CellEntity>

    @Query("SELECT * FROM ${RoomConstants.TABLE_CELLS}")
    fun getAllCellsFlow(): Flow<List<CellEntity>>
}