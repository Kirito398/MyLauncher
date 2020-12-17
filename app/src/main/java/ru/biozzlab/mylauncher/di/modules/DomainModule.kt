package ru.biozzlab.mylauncher.di.modules

import dagger.Module
import dagger.Provides
import ru.biozzlab.mylauncher.domain.interactor.*
import ru.biozzlab.mylauncher.domain.interfaces.Repository

@Module
class DomainModule {

    @Provides
    fun provideLoadCellsUseCase(repository: Repository): LoadCells = LoadCells(repository)

    @Provides
    fun provideLoadWorkSpaceItems(repository: Repository): LoadWorkSpaceItems = LoadWorkSpaceItems(repository)

    @Provides
    fun provideUpdateShortcut(repository: Repository): UpdateShortcut = UpdateShortcut(repository)

    @Provides
    fun provideIsWorkspaceInit(repository: Repository): IsWorkspaceInit = IsWorkspaceInit(repository)

    @Provides
    fun provideSaveShortcuts(repository: Repository): SaveShortcuts = SaveShortcuts(repository)
}