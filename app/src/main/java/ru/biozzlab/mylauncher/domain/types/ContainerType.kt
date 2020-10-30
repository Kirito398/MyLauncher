package ru.biozzlab.mylauncher.domain.types

enum class ContainerType (val id: Int) {
    HOT_SEAT(0),
    DESKTOP(1);

    companion object {
        fun fromID(id: Int): ContainerType? {
            for (type in values())
                if (type.id == id)
                    return type
            return null
        }

        fun getList(): LongArray {
            val list = mutableListOf<Long>()

            for (type in values())
                list.add(type.id.toLong())

            return list.toLongArray()
        }
    }
}