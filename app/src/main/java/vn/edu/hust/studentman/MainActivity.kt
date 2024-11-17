package vn.edu.hust.studentman

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
  private lateinit var studentAdapter: StudentAdapter
  private var deletedStudent: StudentModel? = null
  private var deletedPosition: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val students = mutableListOf(
      StudentModel("Nguyễn Văn An", "SV001"),
      StudentModel("Trần Thị Bảo", "SV002"),
      StudentModel("Lê Hoàng Cường", "SV003"),
      StudentModel("Phạm Thị Dung", "SV004"),
      StudentModel("Đỗ Minh Đức", "SV005"),
      StudentModel("Vũ Thị Hoa", "SV006"),
      StudentModel("Hoàng Văn Hải", "SV007"),
      StudentModel("Bùi Thị Hạnh", "SV008"),
      StudentModel("Đinh Văn Hùng", "SV009"),
      StudentModel("Nguyễn Thị Linh", "SV010"),
      StudentModel("Phạm Văn Long", "SV011"),
      StudentModel("Trần Thị Mai", "SV012"),
      StudentModel("Lê Thị Ngọc", "SV013"),
      StudentModel("Vũ Văn Nam", "SV014"),
      StudentModel("Hoàng Thị Phương", "SV015"),
      StudentModel("Đỗ Văn Quân", "SV016"),
      StudentModel("Nguyễn Thị Thu", "SV017"),
      StudentModel("Trần Văn Tài", "SV018"),
      StudentModel("Phạm Thị Tuyết", "SV019"),
      StudentModel("Lê Văn Vũ", "SV020")
    )

    studentAdapter = StudentAdapter(students) { student, position, action ->
      when (action) {
        "edit" -> showEditDialog(student, position)
        "delete" -> showDeleteDialog(student, position)
      }
    }

    findViewById<RecyclerView>(R.id.recycler_view_students).apply {
      adapter = studentAdapter
      layoutManager = LinearLayoutManager(this@MainActivity)
    }

    findViewById<Button>(R.id.btn_add_new).setOnClickListener {
      showAddDialog()
    }
  }

  private fun showAddDialog() {
    val dialogView = layoutInflater.inflate(R.layout.dialog_student, null)

    AlertDialog.Builder(this)
      .setTitle("Thêm sinh viên mới")
      .setView(dialogView)
      .setPositiveButton("Thêm") { _, _ ->
        val name = dialogView.findViewById<EditText>(R.id.edit_text_name).text.toString()
        val studentId = dialogView.findViewById<EditText>(R.id.edit_text_student_id).text.toString()

        if (name.isNotEmpty() && studentId.isNotEmpty()) {
          val newStudent = StudentModel(name, studentId)
          studentAdapter.addStudent(newStudent)
        }
      }
      .setNegativeButton("Hủy", null)
      .show()
  }

  private fun showEditDialog(student: StudentModel, position: Int) {
    val dialogView = layoutInflater.inflate(R.layout.dialog_student, null)

    dialogView.findViewById<EditText>(R.id.edit_text_name).setText(student.studentName)
    dialogView.findViewById<EditText>(R.id.edit_text_student_id).setText(student.studentId)

    AlertDialog.Builder(this)
      .setTitle("Sửa thông tin sinh viên")
      .setView(dialogView)
      .setPositiveButton("Cập nhật") { _, _ ->
        val name = dialogView.findViewById<EditText>(R.id.edit_text_name).text.toString()
        val studentId = dialogView.findViewById<EditText>(R.id.edit_text_student_id).text.toString()

        if (name.isNotEmpty() && studentId.isNotEmpty()) {
          val updatedStudent = StudentModel(name, studentId)
          studentAdapter.updateStudent(updatedStudent, position)
        }
      }
      .setNegativeButton("Hủy", null)
      .show()
  }

  private fun showDeleteDialog(student: StudentModel, position: Int) {
    AlertDialog.Builder(this)
      .setTitle("Xác nhận xóa")
      .setMessage("Bạn có chắc chắn muốn xóa sinh viên ${student.studentName}?")
      .setPositiveButton("Xóa") { _, _ ->
        deleteStudent(position)
      }
      .setNegativeButton("Hủy", null)
      .show()
  }

  private fun deleteStudent(position: Int) {
    deletedStudent = studentAdapter.removeStudent(position)
    deletedPosition = position

    Snackbar.make(
      findViewById(R.id.main),
      "Đã xóa ${deletedStudent?.studentName}",
      Snackbar.LENGTH_LONG
    ).setAction("Hoàn tác") {
      deletedStudent?.let {
        studentAdapter.addStudent(it)
        deletedStudent = null
        deletedPosition = -1
      }
    }.show()
  }
}
