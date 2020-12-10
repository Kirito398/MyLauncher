package ru.biozzlab.mylauncher.cache

import android.content.Context
import android.util.Xml
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import org.xmlpull.v1.XmlPullParser
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.cache.RoomConstants.DB_VERSION
import ru.biozzlab.mylauncher.cache.daos.CellDao
import ru.biozzlab.mylauncher.cache.entities.CellEntity
import ru.biozzlab.mylauncher.domain.types.ContainerType

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
                                if (context.packageManager.getLaunchIntentForPackage(packageName) == null) continue

                                addCell(db, id, packageName, className, container, cellX, cellY, desktopNumber)
                            }
                        }
                    }).build()
                }
            return INSTANCE!!
        }

        private fun addCell(
            db: SupportSQLiteDatabase,
            id: Int,
            packageName: String,
            className: String,
            container: Int,
            cellX: Int,
            cellY: Int,
            desktopNumber: Int = 0
        ) {
            val sqlRequest = "INSERT INTO ${RoomConstants.TABLE_CELLS} VALUES(" +
                    "${id}, " +
                    "'$packageName', " +
                    "'$className', " +
                    "$container, " +
                    "$cellX, " +
                    "$cellY, " +
                    "$desktopNumber);"

            db.execSQL(sqlRequest)
        }
    }


}