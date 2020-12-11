package ru.biozzlab.mylauncher.di.modules

import dagger.Module
import dagger.Provides
import ru.biozzlab.mylauncher.domain.interactor.IsWorkspaceInit
import ru.biozzlab.mylauncher.domain.interactor.LoadCells
import ru.biozzlab.mylauncher.domain.interactor.SaveShortcuts
import ru.biozzlab.mylauncher.domain.interactor.UpdateShortcut
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.biozzlab.mylauncher.presenters.LauncherPresenter

@Module
class PresentationModule {

    @Provides
    fun provideLauncherPresenter(loadCells: LoadCells, updateShortcut: UpdateShortcut, isWorkspaceInit: IsWorkspaceInit, saveShortcuts: SaveShortcuts): LauncherViewContract.Presenter
            = LauncherPresenter(loadCells, updateShortcut, isWorkspaceInit, saveShortcuts)
}