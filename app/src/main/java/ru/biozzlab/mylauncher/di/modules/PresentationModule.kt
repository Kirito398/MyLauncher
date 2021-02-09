package ru.biozzlab.mylauncher.di.modules

import dagger.Module
import dagger.Provides
import ru.biozzlab.mylauncher.domain.interactor.*
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.biozzlab.mylauncher.presenters.LauncherPresenter

@Module
class PresentationModule {

    @Provides
    fun provideLauncherPresenter(loadWorkSpaceItems: LoadWorkSpaceItems, updateCell: UpdateCell, isWorkspaceInit: IsWorkspaceInit, saveCells: SaveCells, insertCell: InsertCell): LauncherViewContract.Presenter
            = LauncherPresenter(loadWorkSpaceItems, updateCell, isWorkspaceInit, saveCells, insertCell)
}