package ru.biozzlab.mylauncher.cache

import android.content.ContentValues
import android.content.Context
import android.content.res.TypedArray
import android.database.sqlite.SQLiteDatabase
import android.util.Xml
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import org.xmlpull.v1.XmlPullParser
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.cache.RoomConstants.APP_WIDGET
import ru.biozzlab.mylauncher.cache.RoomConstants.DB_VERSION
import ru.biozzlab.mylauncher.cache.RoomConstants.SHORTCUT
import ru.biozzlab.mylauncher.cache.RoomConstants.TABLE_CELLS
import ru.biozzlab.mylauncher.cache.RoomConstants.TAG_CELL_X
import ru.biozzlab.mylauncher.cache.RoomConstants.TAG_CELL_Y
import ru.biozzlab.mylauncher.cache.RoomConstants.TAG_CLASS_NAME
import ru.biozzlab.mylauncher.cache.RoomConstants.TAG_CONTAINER
import ru.biozzlab.mylauncher.cache.RoomConstants.TAG_DESKTOP_NUMBER
import ru.biozzlab.mylauncher.cache.RoomConstants.TAG_ID
import ru.biozzlab.mylauncher.cache.RoomConstants.TAG_ITEM_TYPE
import ru.biozzlab.mylauncher.cache.RoomConstants.TAG_PACKAGE_NAME
import ru.biozzlab.mylauncher.cache.RoomConstants.TAG_SPAN_X
import ru.biozzlab.mylauncher.cache.RoomConstants.TAG_SPAN_Y
import ru.biozzlab.mylauncher.cache.daos.CellDao
import ru.biozzlab.mylauncher.cache.entities.CellEntity
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType

@Database(entities = [CellEntity::class], version = DB_VERSION)
abstract class RoomManager : RoomDatabase() {
    abstract fun cellDao(): CellDao

    companion object {
        private var INSTANCE: RoomManager? = null

        fun getClient(context: Context): RoomManager {
            if (INSTANCE == null)
                synchronized(RoomManager::class) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        RoomManager::class.java,
                        RoomConstants.DB_NAME
                    ).addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            val parser = context.resources.getXml(R.xml.default_workspace)
                            val attributeSet = Xml.asAttributeSet(parser)

                            var type: Int
                            val depth = parser.depth
                            while ((parser.next().also { type = it } != XmlPullParser.END_TAG || parser.depth > depth)
                                && type != XmlPullParser.END_DOCUMENT) {
                                if (type != XmlPullParser.START_TAG) continue

                                val name = parser.name ?: continue

                                val attrs = context.obtainStyledAttributes(
                                    attributeSet,
                                    R.styleable.Favorite
                                )

                                val container = attrs.getInt(
                                    R.styleable.Favorite_container,
                                    ContainerType.DESKTOP.id
                                )
                                val packageName =
                                    attrs.getString(R.styleable.Favorite_packageName) ?: continue
                                val className =
                                    attrs.getString(R.styleable.Favorite_className) ?: continue
                                val id = attrs.getInt(R.styleable.Favorite_id, -1)
                                val cellX = attrs.getInt(R.styleable.Favorite_x, -1)
                                val cellY = attrs.getInt(R.styleable.Favorite_y, -1)
                                val desktopNumber = attrs.getInt(R.styleable.Favorite_desktopNumber, 0)

                                if (id == -1 || cellX == -1 || cellY == -1) continue

                                val spanX = attrs.getInt(R.styleable.Favorite_spanX, 1)
                                val spanY = attrs.getInt(R.styleable.Favorite_spanY, 1)

                                val values = ContentValues()
                                values.put(TAG_ID, id)
                                values.put(TAG_PACKAGE_NAME, packageName)
                                values.put(TAG_CLASS_NAME, className)
                                values.put(TAG_CONTAINER, container)
                                values.put(TAG_CELL_X, cellX)
                                values.put(TAG_CELL_Y, cellY)
                                values.put(TAG_DESKTOP_NUMBER, desktopNumber)
                                values.put(TAG_SPAN_X, spanX)
                                values.put(TAG_SPAN_Y, spanY)

                                addInDb(name, values, db, context, attrs)
                            }
                        }
                    }).build()
                }
            return INSTANCE!!
        }

        private fun addInDb(name: String, values: ContentValues, db: SupportSQLiteDatabase, context: Context, attrs: TypedArray) {
            when (name) {
                SHORTCUT -> addShortcut(context, db, values)
                APP_WIDGET -> { addAppWidget(context, db, values, attrs) }
            }
        }

        private fun addShortcut(context: Context, db: SupportSQLiteDatabase, values: ContentValues) {
            if (context.packageManager.getLaunchIntentForPackage(values.getAsString(TAG_PACKAGE_NAME)) == null) return
            values.put(TAG_ITEM_TYPE, WorkspaceItemType.SHORTCUT.type)
            db.insert(TABLE_CELLS, SQLiteDatabase.CONFLICT_REPLACE, values)
        }

        private fun addAppWidget(context: Context, db: SupportSQLiteDatabase, values: ContentValues, attrs: TypedArray) {
            values.put(TAG_ITEM_TYPE, WorkspaceItemType.WIDGET.type)
            db.insert(TABLE_CELLS, SQLiteDatabase.CONFLICT_REPLACE, values)
        }
    }
}