package ru.biozzlab.mylauncher.domain.interactor

import kotlinx.coroutines.flow.Flow
import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.sir.core.FlowUseCase
import ru.sir.core.None

class WorkSpaceItems(private val repository: Repository) : FlowUseCase<MutableList<ItemCell>, None>() {
    override fun run(params: None): Flow<MutableList<ItemCell>> = repository.cells()
}