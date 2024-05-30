package com.hoangdoviet.finaldoan.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Adds a text watcher to the TextInputLayout's EditText to clear its error message when its text changes.
 * Thêm trình theo dõi văn bản vào EditText của TextInputLayout để xóa thông báo lỗi khi văn bản thay đổi.
 */
fun TextInputLayout.addTextWatcher() {
    editText?.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            error = null
        }
    })
}
/**
 * Clears the text of the TextInputEditText and removes its focus.
 * Xóa văn bản của TextInputEditText và xóa tiêu điểm của nó.
 */
fun TextInputEditText.clearText() {
    setText("")
    clearFocus()
}
/**
 * Trả về giá trị đầu vào đã được cắt bớt của TextInputEditText dưới dạng Chuỗi.
 *
 * @return Giá trị đầu vào của TextInputEditText dưới dạng Chuỗi.
 */
fun TextInputEditText.getInputValue(): String {
    return text.toString().trim()
}
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}