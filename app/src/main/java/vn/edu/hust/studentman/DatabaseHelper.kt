package vn.edu.hust.studentman

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Lớp quản lý kết nối DB và CRUD
class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "StudentDB"
        private const val DATABASE_VERSION = 1

        // Tên bảng và các cột
        private const val TABLE_STUDENT = "student"
        private const val COLUMN_ID = "id"
        private const val COLUMN_STUDENT_NAME = "studentName"
        private const val COLUMN_STUDENT_ID = "studentId"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Tạo bảng student (id - khóa chính tự tăng, studentName, studentId)
        val createTableQuery = """
            CREATE TABLE $TABLE_STUDENT (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_STUDENT_NAME TEXT,
                $COLUMN_STUDENT_ID TEXT
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Xóa bảng cũ nếu tồn tại và tạo lại
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENT")
        onCreate(db)
    }

    // Thêm 1 student mới
    fun addStudent(student: StudentModel): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_STUDENT_NAME, student.studentName)
            put(COLUMN_STUDENT_ID, student.studentId)
        }
        val result = db.insert(TABLE_STUDENT, null, contentValues)
        db.close()
        return result
    }

    // Lấy tất cả student từ DB
    fun getAllStudents(): MutableList<StudentModel> {
        val studentList = mutableListOf<StudentModel>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_STUDENT"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_NAME))
                val studentId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID))
                studentList.add(StudentModel(name, studentId))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return studentList
    }

    // Cập nhật student (tìm theo studentId cũ)
    fun updateStudent(oldStudentId: String, newStudent: StudentModel): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_STUDENT_NAME, newStudent.studentName)
            put(COLUMN_STUDENT_ID, newStudent.studentId)
        }
        // Trả về số bản ghi cập nhật được
        val result = db.update(
            TABLE_STUDENT,
            contentValues,
            "$COLUMN_STUDENT_ID = ?",
            arrayOf(oldStudentId)
        )
        db.close()
        return result
    }

    // Xóa student (tìm theo studentId)
    fun deleteStudent(studentId: String): Int {
        val db = writableDatabase
        val result = db.delete(
            TABLE_STUDENT,
            "$COLUMN_STUDENT_ID = ?",
            arrayOf(studentId)
        )
        db.close()
        return result
    }
}
