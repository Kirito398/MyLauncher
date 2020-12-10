package ru.biozzlab.mylauncher.cache

import android.content.SharedPreferences
import ru.biozzlab.mylauncher.App
import ru.bis.entities.Either
import ru.bis.entities.None
import javax.inject.Inject

class SharedPrefsManager @Inject constructor(private val prefs: SharedPreferences) {
    companion object {
        const val WORKSPACE_IS_INIT = "workspace_is_init"
    }

    fun getIsWorkspaceInit(): Either<None, Boolean> = Either.Right(prefs.contains(App.WORKSPACE_IS_INIT))
}