package vn.edu.hust.studentman

import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
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

  // Thêm biến dbHelper để thao tác với SQLite
  private lateinit var dbHelper: DatabaseHelper

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Khởi tạo DatabaseHelper
    dbHelper = DatabaseHelper(this)

    // Thay vì tạo list cứng, ta lấy sinh viên từ DB
    val students = dbHelper.getAllStudents()

    // Tạo adapter với danh sách sinh viên
    studentAdapter = StudentAdapter(students) { student, position, action ->
      when (action) {
        "edit" -> showEditDialog(student, position)
        "delete" -> showDeleteDialog(student, position)
      }
    }

    // Gán adapter vào RecyclerView
    findViewById<RecyclerView>(R.id.recycler_view_students).apply {
      adapter = studentAdapter
      layoutManager = LinearLayoutManager(this@MainActivity)
    }

    // Xử lý nút "Add new"
    findViewById<Button>(R.id.btn_add_new).setOnClickListener {
      showAddDialog()
    }
  }

  // Hiển thị Dialog để thêm sinh viên mới
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
          // Thêm vào DB
          val rowId = dbHelper.addStudent(newStudent)
          // Nếu thêm thành công, rowId > 0
          if (rowId > 0) {
            // Cập nhật adapter
            studentAdapter.addStudent(newStudent)
          }
        }
      }
      .setNegativeButton("Hủy", null)
      .show()
  }

  // Hiển thị Dialog để sửa thông tin sinh viên
  private fun showEditDialog(student: StudentModel, position: Int) {
    val dialogView = layoutInflater.inflate(R.layout.dialog_student, null)
    val editName = dialogView.findViewById<EditText>(R.id.edit_text_name)
    val editStudentId = dialogView.findViewById<EditText>(R.id.edit_text_student_id)

    // Điền sẵn thông tin
    editName.setText(student.studentName)
    editStudentId.setText(student.studentId)

    AlertDialog.Builder(this)
      .setTitle("Sửa thông tin sinh viên")
      .setView(dialogView)
      .setPositiveButton("Cập nhật") { _, _ ->
        val name = editName.text.toString()
        val newStudentId = editStudentId.text.toString()
        val oldStudentId = student.studentId  // Lưu ID cũ

        if (name.isNotEmpty() && newStudentId.isNotEmpty()) {
          val updatedStudent = StudentModel(name, newStudentId)
          // Cập nhật trên DB (tìm theo oldStudentId)
          val rowsAffected = dbHelper.updateStudent(oldStudentId, updatedStudent)
          if (rowsAffected > 0) {
            // Nếu thành công, cập nhật adapter
            studentAdapter.updateStudent(updatedStudent, position)
          }
        }
      }
      .setNegativeButton("Hủy", null)
      .show()
  }

  // Hiển thị Dialog xác nhận xóa
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

  // Xóa sinh viên
  private fun deleteStudent(position: Int) {
    val studentToDelete = studentAdapter.getStudent(position)
    // Xóa trong DB (tìm theo studentId)
    val rowsDeleted = dbHelper.deleteStudent(studentToDelete.studentId)
    if (rowsDeleted > 0) {
      // Chỉ xóa trên adapter nếu DB xóa thành công
      deletedStudent = studentAdapter.removeStudent(position)
      deletedPosition = position

      Snackbar.make(
        findViewById(R.id.main),
        "Đã xóa ${deletedStudent?.studentName}",
        Snackbar.LENGTH_LONG
      ).setAction("Hoàn tác") {
        deletedStudent?.let {
          // Chèn lại vào DB nếu người dùng chọn “Hoàn tác”
          val rowId = dbHelper.addStudent(it)
          if (rowId > 0) {
            studentAdapter.addStudent(it)
          }
          deletedStudent = null
          deletedPosition = -1
        }
      }.show()
    }
  }

  // Tạo menu option
  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.option_menu, menu)
    return true
  }

  // Xử lý item được chọn trong menu
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.menu_add_new -> {
        showAddDialog()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  // Tạo menu context (nếu bạn dùng context menu)
  override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
    super.onCreateContextMenu(menu, v, menuInfo)
    menuInflater.inflate(R.menu.context_menu, menu)
  }

  // Xử lý item được chọn trong context menu
  override fun onContextItemSelected(item: MenuItem): Boolean {
    val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
    val position = info.position
    return when (item.itemId) {
      R.id.menu_edit -> {
        val student = studentAdapter.getStudent(position)
        showEditDialog(student, position)
        true
      }
      R.id.menu_remove -> {
        val student = studentAdapter.getStudent(position)
        showDeleteDialog(student, position)
        true
      }
      else -> super.onContextItemSelected(item)
    }
  }
}
