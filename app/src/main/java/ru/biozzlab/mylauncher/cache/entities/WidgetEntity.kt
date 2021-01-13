package ru.biozzlab.mylauncher.cache.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.biozzlab.mylauncher.cache.RoomConstants

@Entity(tableName = RoomConstants.TABLE_WIDGETS)
data class WidgetEntity(
    @ColumnInfo(name = RoomConstants.TAG_PACKAGE_NAME)
    val packageName: String,
    @ColumnInfo(name = RoomConstants.TAG_CLASS_NAME)
    val className: String,
    @ColumnInfo(name = RoomConstants.TAG_CONTAINER)
    val container: Int,
    @ColumnInfo(name = RoomConstants.TAG_CELL_X)
    val cellX: Int,
    @ColumnInfo(name = RoomConstants.TAG_CELL_Y)
    val cellY: Int,
    @ColumnInfo(name = RoomConstants.TAG_DESKTOP_NUMBER)
    val desktopNumber: Int,
    @ColumnInfo(name = RoomConstants.TAG_SPAN_X)
    val spanX: Int,
    @ColumnInfo(name = RoomConstants.TAG_SPAN_Y)
    val spanY: Int
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = RoomConstants.TAG_ID)
    var id: Long = 0
}