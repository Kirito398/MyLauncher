package ru.biozzlab.mylauncher.di.modules

import dagger.Module
import dagger.Provides
import ru.biozzlab.mylauncher.domain.interactor.IsWorkspaceInit
import ru.biozzlab.mylauncher.domain.interactor.LoadCells
import ru.biozzlab.mylauncher.domain.interactor.SaveShortcuts
import ru.biozzlab.mylauncher.domain.interactor.UpdateShortcut
import ru.biozzlab.mylauncher.domain.interfaces.Repository

@Module
class DomainModule {

    @Provides
    fun provideLoadCellsUseCase(repository: Repository): LoadCells = LoadCells(repository)

    @Provides
    fun provideUpdateShortcut(repository: Repository): UpdateShortcut = UpdateShortcut(repository)

    @Provides
    fun provideIsWorkspaceInit(repository: Repository): IsWorkspaceInit = IsWorkspaceInit(repository)

    @Provides
    fun provideSaveShortcuts(repository: Repository): SaveShortcuts = SaveShortcuts(repository)
}