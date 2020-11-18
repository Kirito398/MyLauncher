package ru.biozzlab.mylauncher.domain.models

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.os.UserHandle
import android.os.UserManager
import ru.biozzlab.mylauncher.App
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.ui.views.IconDrawable

class ItemShortcut(cell: ItemCell) : ItemCell(
    cell.id,
    cell.container,
    cell.packageName,
    cell.className,
    cell.cellX,
    cell.cellY,
    cell.desktopNumber,
    cell.cellHSpan,
    cell.cellVSpan
) {
    lateinit var intent: Intent
    var icon: IconDrawable? = null
    var title: String = ""

    init {
        createIntent(cell.packageName, cell.className)
        initIcon()
    }

    private fun createIntent(packageName: String, className: String) {
        intent = Intent(Intent.ACTION_MAIN, null)
        val componentName = ComponentName(packageName, className)

        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.component = componentName
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
    }

    private fun initIcon() {
        val userManager = App.appContext.getSystemService(Context.USER_SERVICE) as UserManager
        val users = userManager.userProfiles

        var currentUser: UserHandle? = null
        for (user in users) {
            if (user != null)
                currentUser = user
        }

        val launcherApps= App.appContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val launcherActivityInfo = launcherApps.resolveActivity(intent, currentUser)
        val iconDensity = (App.appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).launcherLargeIconDensity

        launcherActivityInfo?.run {
            icon = IconDrawable(createIconBitmap(getBadgedIcon(iconDensity)))
        }
    }

    private fun createIconBitmap(icon: Drawable): Bitmap {
        var width: Int = App.appContext.resources.getDimension(R.dimen.app_icon_size).toInt()
        var height: Int = width
        val textureWidth = width
        val textureHeight = width

        when(icon) {
            is PaintDrawable -> {
                icon.intrinsicWidth = width
                icon.intrinsicHeight = height
            }
            is BitmapDrawable -> {
                val bitmap = icon.bitmap
                if (bitmap.density == Bitmap.DENSITY_NONE)
                    icon.setTargetDensity(App.appContext.resources.displayMetrics)
            }
        }

        val sourceWidth: Int = icon.intrinsicWidth
        val sourceHeight: Int = icon.intrinsicHeight

        if (sourceWidth > 0 && sourceHeight > 0) {
            when {
                width < sourceWidth || height < sourceHeight -> {
                    val ratio = sourceWidth / sourceHeight
                    when {
                        sourceWidth > sourceHeight -> height = width / ratio
                        sourceWidth < sourceHeight -> width = height * ratio
                    }
                }

                sourceWidth < width && sourceHeight < height -> {
                    width = sourceWidth
                    height = sourceHeight
                }
            }
        }

        val bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas()
        canvas.setBitmap(bitmap)

        val left = (textureWidth - width) / 2
        val top = (textureHeight - height) / 2
        val bounds = Rect()

        bounds.set(icon.bounds)
        icon.setBounds(left, top, left + width, top + height)
        icon.draw(canvas)
        icon.bounds = bounds
        canvas.setBitmap(null)

        return bitmap
    }
}