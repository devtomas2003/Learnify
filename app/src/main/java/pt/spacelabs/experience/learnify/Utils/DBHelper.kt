import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import pt.spacelabs.experience.learnify.Entitys.ClassDetailInfo
import pt.spacelabs.experience.learnify.Entitys.Course
import pt.spacelabs.experience.learnify.Entitys.Playback

class DBHelper(context: Context?) :
    SQLiteOpenHelper(context, DBName, null, 1) {
    override fun onCreate(leanifyDB: SQLiteDatabase) {
        leanifyDB.execSQL("CREATE TABLE configs(configType TEXT PRIMARY KEY, value TEXT)")
        leanifyDB.execSQL("CREATE TABLE courses(id TEXT PRIMARY KEY, name TEXT, description TEXT, FriendlyName TEXT, ImagePath TEXT, UUID TEXT, isEnable TEXT)")
        leanifyDB.execSQL("CREATE TABLE classes(id TEXT PRIMARY KEY, courseId TEXT, name TEXT, description TEXT, path TEXT, ImagePath TEXT, UUID TEXT, isEnable TEXT)")
        leanifyDB.execSQL("CREATE TABLE offlinePlayback(id TEXT PRIMARY KEY, classId TEXT, chunk TEXT)")
    }

    override fun onUpgrade(leanifyDB: SQLiteDatabase, i: Int, i1: Int) {
        leanifyDB.execSQL("DROP TABLE IF EXISTS configs")
        leanifyDB.execSQL("DROP TABLE IF EXISTS courses")
        leanifyDB.execSQL("DROP TABLE IF EXISTS classes")
        leanifyDB.execSQL("DROP TABLE IF EXISTS offlinePlayback")
    }

    fun createConfig(configType: String?, value: String?) {
        val leanifyDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("configType", configType)
        contentValues.put("value", value)
        val result = leanifyDB.insert("configs", null, contentValues)
    }

    fun getConfig(configType: String): String {
        val leanifyDB = this.readableDatabase
        val cursor = leanifyDB.rawQuery(
            "SELECT * FROM configs WHERE configType = ?",
            arrayOf(configType)
        )

        var result = "none"
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow("value"))
        }

        cursor.close()
        return result
    }

    fun updateConfig(configType: String, value: String) {
        val leanifyDB = this.readableDatabase
        val con = ContentValues()
        con.put("value", value)
        leanifyDB.update("configs", con, "configType = ?", arrayOf(configType))
    }

    fun clearConfig(configType: String) {
        val leanifyDB = this.writableDatabase
        leanifyDB.execSQL("DELETE FROM configs WHERE configType = ?", arrayOf(configType))
    }

    fun createCourse(id: String, name: String, description: String, friendlyname: String, imagePath: String, UUID: String, isEnable: Boolean){
        val leanifyDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("id", id)
        contentValues.put("name", name)
        contentValues.put("description", description)
        contentValues.put("FriendlyName", friendlyname)
        contentValues.put("ImagePath", imagePath)
        contentValues.put("UUID", UUID)
        contentValues.put("isEnable", isEnable)
        val result = leanifyDB.insert("courses", null, contentValues)
    }

    fun getCourses(): List<Course> {
        val courses = mutableListOf<Course>()
        val query = "SELECT id, FriendlyName, name, description, ImagePath, UUID, isEnable FROM courses WHERE isEnable = 1"
        val cursor: Cursor = this.writableDatabase.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                val friendlyName = cursor.getString(cursor.getColumnIndexOrThrow("FriendlyName"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val poster = cursor.getString(cursor.getColumnIndexOrThrow("ImagePath"))
                val uuid = cursor.getString(cursor.getColumnIndexOrThrow("UUID"))
                val isEnable = cursor.getString(cursor.getColumnIndexOrThrow("isEnable"))

                courses.add(Course(id, friendlyName, title, description, poster, uuid, isEnable.toBoolean()))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return courses
    }

    fun getCourseFriendlyName(friendlyName: String): Course? {
        val query = "SELECT id, FriendlyName, name, description, ImagePath, UUID, isEnable FROM courses WHERE FriendlyName = ? AND isEnable = 1"
        val cursor: Cursor = this.readableDatabase.rawQuery(query, arrayOf(friendlyName))

        var course: Course? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val poster = cursor.getString(cursor.getColumnIndexOrThrow("ImagePath"))
            val uuid = cursor.getString(cursor.getColumnIndexOrThrow("UUID"))
            val isEnable = cursor.getString(cursor.getColumnIndexOrThrow("isEnable"))

            course = Course(id, friendlyName, title, description, poster, uuid, isEnable.toBoolean())
        }
        cursor.close()
        return course
    }

    fun getCourseById(courseId: String): Course? {
        val query = "SELECT id, FriendlyName, name, description, ImagePath, UUID, isEnable FROM courses WHERE id = ? AND isEnable = 1"
        val cursor: Cursor = this.readableDatabase.rawQuery(query, arrayOf(courseId))

        var course: Course? = null
        if (cursor.moveToFirst()) {
            val friendlyName = cursor.getString(cursor.getColumnIndexOrThrow("FriendlyName"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val poster = cursor.getString(cursor.getColumnIndexOrThrow("ImagePath"))
            val uuid = cursor.getString(cursor.getColumnIndexOrThrow("UUID"))
            val isEnable = cursor.getString(cursor.getColumnIndexOrThrow("isEnable"))

            course = Course(courseId, friendlyName, title, description, poster, uuid, isEnable.toBoolean())
        }
        cursor.close()
        return course
    }

    fun deleteCourse(courseId: String) {
        val leanifyDB = this.writableDatabase
        leanifyDB.execSQL("DELETE FROM courses WHERE id = ?", arrayOf(courseId))
    }

    fun updateCourse(courseId: String, name: String, description: String, friendlyname: String, imagePath: String, uuid: String, isEnable: Boolean) {
        val leanifyDB = this.readableDatabase
        val con = ContentValues()
        con.put("name", name)
        con.put("description", description)
        con.put("FriendlyName", friendlyname)
        con.put("ImagePath", imagePath)
        con.put("UUID", uuid)
        con.put("isEnable", isEnable)
        leanifyDB.update("courses", con, "id = ?", arrayOf(courseId))
    }

    fun createClass(id: String, courseId: String, name: String, description: String, path: String, imagePath: String, UUID: String, isEnable: Boolean){
        val leanifyDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("id", id)
        contentValues.put("courseId", courseId)
        contentValues.put("name", name)
        contentValues.put("description", description)
        contentValues.put("path", path)
        contentValues.put("ImagePath", imagePath)
        contentValues.put("UUID", UUID)
        contentValues.put("isEnable", isEnable)
        val result = leanifyDB.insert("classes", null, contentValues)
    }

    fun getClasses(): List<ClassDetailInfo> {
        val classes = mutableListOf<ClassDetailInfo>()
        val query = "SELECT id, courseId, name, description, path, ImagePath, UUID, isEnable FROM classes WHERE isEnable = 1"
        val cursor: Cursor = this.writableDatabase.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                val courseId = cursor.getString(cursor.getColumnIndexOrThrow("courseId"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val path = cursor.getString(cursor.getColumnIndexOrThrow("path"))
                val ImagePath = cursor.getString(cursor.getColumnIndexOrThrow("ImagePath"))
                val uuid = cursor.getString(cursor.getColumnIndexOrThrow("UUID"))
                val isEnable = cursor.getString(cursor.getColumnIndexOrThrow("isEnable"))

                classes.add(ClassDetailInfo(id, courseId, name, description, path, ImagePath, uuid, isEnable.toBoolean()))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return classes
    }

    fun getClassesByCourse(courseId: String): List<ClassDetailInfo> {
        val classes = mutableListOf<ClassDetailInfo>()
        val query = "SELECT id, courseId, name, description, path, ImagePath, UUID, isEnable FROM classes WHERE courseId = ? AND isEnable = 1"
        val cursor: Cursor = this.readableDatabase.rawQuery(query, arrayOf(courseId))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                val courseId = cursor.getString(cursor.getColumnIndexOrThrow("courseId"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val path = cursor.getString(cursor.getColumnIndexOrThrow("path"))
                val ImagePath = cursor.getString(cursor.getColumnIndexOrThrow("ImagePath"))
                val uuid = cursor.getString(cursor.getColumnIndexOrThrow("UUID"))
                val isEnable = cursor.getString(cursor.getColumnIndexOrThrow("isEnable"))

                classes.add(ClassDetailInfo(id, courseId, name, description, path, ImagePath, uuid, isEnable.toBoolean()))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return classes
    }

    fun getClassById(classId: String): ClassDetailInfo? {
        val query = "SELECT id, courseId, name, description, path, ImagePath, UUID, isEnable FROM classes WHERE id = ? AND isEnable = 1"
        val cursor: Cursor = this.readableDatabase.rawQuery(query, arrayOf(classId))

        var classDetail: ClassDetailInfo? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val courseId = cursor.getString(cursor.getColumnIndexOrThrow("courseId"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val path = cursor.getString(cursor.getColumnIndexOrThrow("path"))
            val ImagePath = cursor.getString(cursor.getColumnIndexOrThrow("ImagePath"))
            val uuid = cursor.getString(cursor.getColumnIndexOrThrow("UUID"))
            val isEnable = cursor.getString(cursor.getColumnIndexOrThrow("isEnable"))

            classDetail = ClassDetailInfo(id, courseId, name, description, path, ImagePath, uuid, isEnable.toBoolean())
        }
        cursor.close()
        return classDetail
    }

    fun deleteClass(classId: String) {
        val leanifyDB = this.writableDatabase
        leanifyDB.execSQL("DELETE FROM classes WHERE id = ?", arrayOf(classId))
    }

    fun updateClass(id: String, courseId: String, name: String, description: String, path: String, imagePath: String, UUID: String, isEnable: Boolean) {
        val leanifyDB = this.readableDatabase
        val con = ContentValues()
        con.put("id", id)
        con.put("courseId", courseId)
        con.put("name", name)
        con.put("description", description)
        con.put("path", path)
        con.put("ImagePath", imagePath)
        con.put("UUID", UUID)
        con.put("isEnable", isEnable)
        leanifyDB.update("classes", con, "id = ?", arrayOf(courseId))
    }

    fun createChunk(id: String, classId: String, chunk: String){
        val leanifyDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("id", id)
        contentValues.put("classId", classId)
        contentValues.put("chunk", chunk)
        val result = leanifyDB.insert("offlinePlayback", null, contentValues)
    }

    fun deleteChunks(classId: String) {
        val leanifyDB = this.writableDatabase
        leanifyDB.execSQL("DELETE FROM offlinePlayback WHERE classId = ?", arrayOf(classId))
    }

    fun getChunksByClassID(classId: String): List<Playback> {
        val playbacks = mutableListOf<Playback>()
        val query = "SELECT id, classId, chunk FROM offlinePlayback WHERE classId = ?"
        val cursor: Cursor = this.readableDatabase.rawQuery(query, arrayOf(classId))

        if (cursor.moveToFirst()) {
            do {
                val chunk = cursor.getString(cursor.getColumnIndexOrThrow("chunk"))

                playbacks.add(Playback(classId, classId, chunk))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return playbacks
    }

    companion object {
        const val DBName: String = "learnify.db"
    }
}