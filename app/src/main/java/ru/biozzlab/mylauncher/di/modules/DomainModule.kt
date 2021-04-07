package ru.biozzlab.mylauncher.di.modules

import dagger.Module
import dagger.Provides
import ru.biozzlab.mylauncher.domain.interactor.*
import ru.biozzlab.mylauncher.domain.interfaces.Repository

@Module
class DomainModule {

    @Provides
    fun provideLoadWorkSpaceItems(repository: Repository): LoadWorkSpaceItems = LoadWorkSpaceItems(repository)

    @Provides
    fun provideUpdateShortcut(repository: Repository): UpdateCell = UpdateCell(repository)

    @Provides
    fun provideUpdateCells(repository: Repository): UpdateCells = UpdateCells(repository)

    @Provides
    fun provideIsWorkspaceInit(repository: Repository): IsWorkspaceInit = IsWorkspaceInit(repository)

    @Provides
    fun provideSaveShortcuts(repository: Repository): SaveCells = SaveCells(repository)

    @Provides
    fun provideInsertCell(repository: Repository): InsertCell = InsertCell(repository)

    @Provides
    fun provideDeleteCell(repository: Repository): DeleteCell = DeleteCell(repository)

    @Provides
    fun provideWorkspaceItems(repository: Repository): WorkSpaceItems = WorkSpaceItems(repository)
}