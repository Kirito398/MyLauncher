package ru.biozzlab.mylauncher.domain.types

enum class TouchStates(val id: Int) {
    REST(0),
    SCROLLING(1),
    PREV_PAGE(2),
    NEXT_PAGE(3);
}