package ru.biozzlab.mylauncher.domain.models

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
    cell.cellVSpan,
    cell.appWidgetId
) {
}