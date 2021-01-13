package ru.biozzlab.mylauncher.domain.types

enum class WorkspaceItemType(val type: Int) {
    SHORTCUT(0),
    WIDGET(1);

    companion object {
        fun fromID(id: Int): WorkspaceItemType? {
            for (type in values())
                if (type.type == id)
                    return type
            return null
        }

        fun getList(): LongArray {
            val list = mutableListOf<Long>()

            for (type in values())
                list.add(type.type.toLong())

            return list.toLongArray()
        }
    }
}