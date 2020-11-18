package ru.biozzlab.mylauncher.cache.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.biozzlab.mylauncher.cache.RoomConstants

@Entity(tableName = RoomConstants.TABLE_CELLS)
data class CellEntity(
    val packageName: String,
    val className: String,
    val container: Int,
    val cellX: Int,
    val cellY: Int,
    val desktopNumber: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}