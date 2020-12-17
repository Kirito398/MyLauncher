package ru.biozzlab.mylauncher.di.modules

import dagger.Module
import dagger.Provides
import ru.biozzlab.mylauncher.domain.interactor.*
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract
import ru.biozzlab.mylauncher.presenters.LauncherPresenter

@Module
class PresentationModule {

    @Provides
    fun provideLauncherPresenter(loadWorkSpaceItems: LoadWorkSpaceItems, updateShortcut: UpdateShortcut, isWorkspaceInit: IsWorkspaceInit, saveShortcuts: SaveShortcuts): LauncherViewContract.Presenter
            = LauncherPresenter(loadWorkSpaceItems, updateShortcut, isWorkspaceInit, saveShortcuts)
}