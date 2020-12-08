package ru.biozzlab.mylauncher.cache.daos

import androidx.room.*
import ru.biozzlab.mylauncher.cache.RoomConstants
import ru.biozzlab.mylauncher.cache.entities.CellEntity

@Dao
interface CellDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cell: CellEntity)

    @Update
    fun update(cell: CellEntity)

    @Delete
    fun delete(cell: CellEntity)

    @Query("SELECT * FROM ${RoomConstants.TABLE_CELLS}")
    fun getAllCells(): List<CellEntity>
}