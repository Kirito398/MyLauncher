package ru.biozzlab.mylauncher.presenters

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.UserHandle
import android.os.UserManager
import ru.biozzlab.mylauncher.App
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.interfaces.LauncherViewContract

class LauncherPresenter : LauncherViewContract.Presenter {
    private lateinit var view: LauncherViewContract.View

    override fun setView(view: LauncherViewContract.View) {
        this.view = view
    }

    override fun init() {
        view.checkForLocaleChanged()
        view.setContentView()
        view.initViews()

        loadShortcuts()
    }

    private fun loadShortcuts() {
        val intent = getAppIntent()
        val launcherApps= App.appContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val iconDensity = (App.appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).launcherLargeIconDensity
        val userManager = App.appContext.getSystemService(Context.USER_SERVICE) as UserManager
        val users = userManager.userProfiles

        var currentUser: UserHandle? = null
        for (user in users) {
            if (user != null)
                currentUser = user
        }

        val launcherActivityInfo = launcherApps.resolveActivity(intent, currentUser)

        val item = ItemShortcut(
            ContainerType.HOT_SEAT,
            4,
            0,
            1,
            1,
            launcherActivityInfo.getBadgedIcon(iconDensity),
            intent,
            currentUser!!
        )

        view.addShortcut(item)
    }

    private fun getAppIntent(): Intent {
        val intent = Intent(Intent.ACTION_MAIN, null)
        val componentName = ComponentName("com.android.browser", "com.android.browser.BrowserActivity")

        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.component = componentName
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED

        return intent
    }
}