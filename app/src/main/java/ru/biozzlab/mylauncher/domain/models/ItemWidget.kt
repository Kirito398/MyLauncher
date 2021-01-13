package ru.biozzlab.mylauncher.domain.models

import android.os.Parcel
import android.os.Parcelable
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType

class ItemWidget(cell: ItemCell) : ItemCell(
    cell.id,
    cell.type,
    cell.container,
    cell.packageName,
    cell.className,
    cell.cellX,
    cell.cellY,
    cell.desktopNumber,
    cell.cellHSpan,
    cell.cellVSpan
), Parcelable {

    constructor(parcel: Parcel) : this(ItemCell (
        parcel.readLong(),
        WorkspaceItemType.fromID(parcel.readInt()) ?: WorkspaceItemType.WIDGET,
        ContainerType.fromID(parcel.readInt()) ?: ContainerType.DESKTOP,
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.apply {
            writeLong(id)
            writeInt(type.type)
            writeInt(container.id)
            writeString(packageName)
            writeString(className)
            writeInt(cellX)
            writeInt(cellY)
            writeInt(desktopNumber)
            writeInt(cellHSpan)
            writeInt(cellVSpan)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemWidget> {
        override fun createFromParcel(parcel: Parcel): ItemWidget {
            return ItemWidget(parcel)
        }

        override fun newArray(size: Int): Array<ItemWidget?> {
            return arrayOfNulls(size)
        }
    }
}