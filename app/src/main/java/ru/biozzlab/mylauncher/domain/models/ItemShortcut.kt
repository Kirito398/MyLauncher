package ru.biozzlab.mylauncher.domain.models

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.os.UserHandle
import android.os.UserManager
import androidx.core.content.ContextCompat
import ru.biozzlab.mylauncher.App
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.ui.views.IconDrawable

class ItemShortcut(cell: ItemCell) : ItemCell(
    cell.id,
    cell.type,
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
    val icon: IconDrawable? get() = initIcon()
    var iconBitmap: Bitmap? = null
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

    private fun initIcon(): IconDrawable? {
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

        return launcherActivityInfo?.run { IconDrawable(createIconBitmap(getIcon(iconDensity))) }
    }

    private fun createIconBitmap(icon: Drawable): Bitmap {
        val resources = App.appContext.resources

        val isHotSeat = container == ContainerType.HOT_SEAT

        val iconBorderRadius = resources.getDimension(if (isHotSeat) R.dimen.hot_seat_app_icon_border_radius else R.dimen.app_icon_border_radius)
        val iconPadding = resources.getDimension(if (isHotSeat) R.dimen.hot_seat_app_icon_padding else R.dimen.app_icon_padding).toInt()
        val iconStroke = resources.getDimension(if (isHotSeat) R.dimen.hot_seat_app_icon_border_stroke else R.dimen.app_icon_border_stroke)
        var width: Int = resources.getDimension(if (isHotSeat) R.dimen.hot_seat_app_icon_size else R.dimen.app_icon_size).toInt()
        var height: Int = width
        val textureWidth = width + iconPadding
        val textureHeight = width + iconPadding

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
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.isAntiAlias = true

        paint.color = ContextCompat.getColor(App.appContext, R.color.app_icon_background_color)
        canvas.drawRoundRect(RectF(0F, 0F, bitmap.width.toFloat(), bitmap.height.toFloat()), iconBorderRadius, iconBorderRadius, paint)

        paint.style = Paint.Style.STROKE
        paint.color = ContextCompat.getColor(App.appContext, R.color.app_icon_background_stroke_color)
        paint.strokeWidth = iconStroke
        canvas.drawRoundRect(RectF(0F, 0F, bitmap.width.toFloat(), bitmap.height.toFloat()), iconBorderRadius, iconBorderRadius, paint)

        if (isHotSeat) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0F)

            val filter = ColorMatrixColorFilter(matrix)
            icon.colorFilter = filter
        }

        val left = (textureWidth - width) / 2
        val top = (textureHeight - height) / 2
        val bounds = Rect()

        bounds.set(icon.bounds)
        icon.setBounds(left, top, left + width, top + height)
        icon.draw(canvas)
        icon.bounds = bounds
        canvas.setBitmap(null)

        iconBitmap = bitmap

        return bitmap
    }
}