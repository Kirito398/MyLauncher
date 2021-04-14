package ru.biozzlab.mylauncher.ui

import android.content.Intent
import androidx.navigation.NavController
import androidx.navigation.findNavController
import ru.biozzlab.mylauncher.R
import ru.sir.presentation.base.BaseActivity
import ru.sir.presentation.navigation.UiAction

class Launcher : BaseActivity() {

    override val layoutId: Int = R.layout.activity_launcher

    override fun getNavController(): NavController = findNavController(R.id.navHostFragment)

    override fun navigateTo(action: UiAction) {
        //TODO("Not yet implemented")
    }

    private var isWorkspaceVisible = true

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { if (intent.hasCategory(Intent.CATEGORY_HOME) && isWorkspaceVisible) super.onBackPressed()}
    }

    override fun onStart() {
        super.onStart()
        isWorkspaceVisible = true
    }

    override fun onStop() {
        super.onStop()
        isWorkspaceVisible = false
    }
}