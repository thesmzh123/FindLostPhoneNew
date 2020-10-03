package device.spotter.finder.appss.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import device.spotter.finder.appss.models.Bookmarks
import device.spotter.finder.appss.utils.Constants.TAGI

class DatabaseHelperUtils(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    companion object {
        private const val DATABASE_NAME = "phoneFinderNewDB"
        private const val TABLE_NAME = "lostphoneloctbl"
        const val COLUMN_TIME = "timed"
        const val COLUMN_LAT = "lat"
        const val COLUMN_LONG = "longi"
        const val COLUMN_IS_SAVED = "isSaved"

        private const val TABLE_NAME4 = "bookmarktbl"
        private const val KEY_ID = "id"
        const val COLUMN_title = "title"
        const val COLUMN_address = "address"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //create table gps tracker
        val CREATE_TABLE_GPS_TRACKER = ("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_IS_SAVED + " TEXT,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_LAT + " TEXT,"
                + COLUMN_LONG + " TEXT);")

        db?.execSQL(CREATE_TABLE_GPS_TRACKER)

        val CREATE_TABLE_BOOKMARk = ("CREATE TABLE " + TABLE_NAME4 + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_title + " TEXT,"
                + COLUMN_address + " TEXT);")

        db?.execSQL(CREATE_TABLE_BOOKMARk)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME4")

        onCreate(db)
    }
    //TODO: add new location to gps

    fun addGpsTrack(isSaved: Int, lat: String, longi: String, time: String) {
        val sqLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_TIME, time)
        contentValues.put(COLUMN_IS_SAVED, isSaved)
        contentValues.put(COLUMN_LAT, lat)
        contentValues.put(COLUMN_LONG, longi)

        val result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues)
        sqLiteDatabase.close()

        if (result > 0) {
            Log.d(TAGI, "inserted")
        } else {
            Log.d(TAGI, "not in serted")
        }
    }

    //TODO: check gps location exist
    fun checkGpsExist(isSaved: Int): Boolean {
        val db = this.writableDatabase

        //Get data from table
        val query = "select * from  " + TABLE_NAME + " where " +
                COLUMN_IS_SAVED + " = " + "'" + isSaved + "'"
        val cursor = db.rawQuery(query, null)

        cursor.moveToFirst()
        if (cursor.count > 0) {

            return true
        }
        cursor.close()
        db.close()

        return false
    }

    //TODO: update gps loc
    fun updateGPSLoc(isSaved: String, lat: String, longi: String, time: String) {
        val sqLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_TIME, time)
        contentValues.put(COLUMN_LAT, lat)
        contentValues.put(COLUMN_LONG, longi)

        val id1 = sqLiteDatabase.update(
            TABLE_NAME,
            contentValues,
            "isSaved = ?",
            arrayOf(isSaved)
        ).toLong()
        if (id1 >= 1) {
            Log.d("Test", "updated")
        } else {
            Log.d("Test", "not updated")

        }
    }

    //TODO: get latitude
    fun getLatitude(isSaved: Int): String {
        var lat = ""
        val db = this.writableDatabase
        val query = "select * from  " + TABLE_NAME + " where " +
                COLUMN_IS_SAVED + " = " + "'" + isSaved + "'"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            lat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAT))
        }
        cursor.close()
        db.close()

        return lat
    }

    //TODO: get longitude
    fun getLongitude(isSaved: Int): String {
        var lat = ""
        val db = this.writableDatabase
        val query = "select * from  " + TABLE_NAME + " where " +
                COLUMN_IS_SAVED + " = " + "'" + isSaved + "'"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            lat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LONG))
        }
        cursor.close()
        db.close()

        return lat
    }

    //TODO: get time
    fun getTime(isSaved: Int): String {
        var lat = ""
        val db = this.writableDatabase
        val query = "select * from  " + TABLE_NAME + " where " +
                COLUMN_IS_SAVED + " = " + "'" + isSaved + "'"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            lat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
        }
        cursor.close()
        db.close()

        return lat
    }

    //TODO: delete location
    fun deleteLocation(isSaved: String): Int? {
        val sqLiteDatabase = this.writableDatabase
        return sqLiteDatabase.delete(TABLE_NAME, "isSaved = ?", arrayOf(isSaved))
    }
    //TODO: check bookmark exist
    fun checkBookmarkExist(title: String): Boolean {
        val db = this.writableDatabase

        //Get data from table
        val query = "select * from  " + TABLE_NAME4 + " where " +
                COLUMN_title + " = " + "'" + title + "'"
        val cursor = db.rawQuery(query, null)

        cursor.moveToFirst()
        if (cursor.count > 0) {

            return true
        }
        cursor.close()
        db.close()

        return false
    }

    //TODO: delete bookmark
    fun deletebookmark(id: String): Int? {
        val sqLiteDatabase = this.writableDatabase
        return sqLiteDatabase.delete(TABLE_NAME4, "id = ?", arrayOf(id))
    }

    //TODO: all bookmarks
    fun getAllBookmarks(): ArrayList<Bookmarks> {
        val db = this.readableDatabase
        val query = "select * from  $TABLE_NAME4"
        val cursor = db.rawQuery(query, arrayOf())
        val messages = ArrayList<Bookmarks>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                messages.add(
                    Bookmarks(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2)
                    )
                )
            }
            if (!cursor.isClosed) cursor.close()
        }
        db.close()
        return messages
    }

    //TODO: add bookmark table
    fun addbookmark(title: String, address: String): Boolean {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(COLUMN_title, title)
        contentValues.put(COLUMN_address, address)

        val result = db.insert(TABLE_NAME4, null, contentValues)

        db.close()
        return result > 0
    }
}