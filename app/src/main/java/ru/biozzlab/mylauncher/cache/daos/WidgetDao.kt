package ru.biozzlab.mylauncher.cache.daos

import androidx.room.*
import ru.biozzlab.mylauncher.cache.RoomConstants
import ru.biozzlab.mylauncher.cache.entities.WidgetEntity

@Dao
interface WidgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(widget: WidgetEntity)

    @Update
    fun update(widget: WidgetEntity)

    @Delete
    fun delete(widget: WidgetEntity)

    @Query("SELECT * FROM ${RoomConstants.TABLE_WIDGETS}")
    fun getAllWidgets(): List<WidgetEntity>
}