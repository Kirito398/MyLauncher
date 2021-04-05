package ru.biozzlab.mylauncher.cache

import android.content.SharedPreferences
import ru.sir.core.Either
import ru.sir.core.None
import javax.inject.Inject

class SharedPrefsManager @Inject constructor(private val prefs: SharedPreferences) {
    companion object {
        const val WORKSPACE_IS_INIT = "workspace_is_init"
    }

    fun getIsWorkspaceInit(): Either<None, Boolean> = Either.Right(prefs.contains(WORKSPACE_IS_INIT))

    fun setIsWorkspaceInit(): Either<None, Boolean> {
        prefs.edit().putBoolean(WORKSPACE_IS_INIT, true).apply()
        return Either.Right(true)
    }
}