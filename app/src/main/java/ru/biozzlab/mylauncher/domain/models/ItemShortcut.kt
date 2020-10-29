package ru.biozzlab.mylauncher.domain.models

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.UserHandle
import ru.biozzlab.mylauncher.domain.types.ContainerType

class ItemShortcut(
    container: ContainerType,
    cellX: Int,
    cellY: Int,
    cellHSpan: Int,
    cellVSpan: Int,
    val icon: Drawable,
    val intent: Intent,
    val user: UserHandle)
    : ItemCell(container, cellX, cellY, cellHSpan, cellVSpan)