package com.hoangdoviet.finaldoan.utils

import java.util.regex.Pattern

class InputValidation {
    companion object {
        private val EMAIL_ADDRESS_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$")
        /**
         * PASSWORD_PATTERN là biểu thức chính quy được sử dụng để kiểm tra các ràng buộc sau đối với mật khẩu:
         * 1. Mật khẩu phải có ít nhất 4 ký tự
         * 2. Mật khẩu phải chứa ít nhất một chữ cái
         * 3. Mật khẩu phải chứa ít nhất một chữ số
         * 4. Mật khẩu phải chứa ít nhất một ký tự đặc biệt trong bộ ký tự được chỉ định
         * Pattern.compile("^.*(?=.{4,})(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!&\$ %&?#_@ ]).*\$") // mẫu mới
         */
        private val PASSWORD_PATTERN =
            Pattern.compile("^.*(?=.{4,})(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!&\$%&?#_@ ]).*\$")
        /**
         * Kiểm tra xem chuỗi đầu vào có rỗng hay rỗng không.
         *
         * @param input Chuỗi cần kiểm tra.
         * @return `true` nếu chuỗi đầu vào không rỗng hoặc trống, ngược lại là `false`.
         */
        fun checkNullity(input: String): Boolean {
            return input.isNotEmpty()
        }
        /**
         * Kiểm tra xem tên người dùng đã cho có hợp lệ không.
         *
         * @param username Tên người dùng để xác thực.
         * @return Một đối tượng [Pair] chứa giá trị boolean cho biết tên người dùng có hợp lệ hay không và một chuỗi
         * thông báo mô tả kết quả xác nhận.
         */
        fun isUsernameValid(username: String): Pair<Boolean, String> {
            if (username.isEmpty()) {
                return Pair(false, "Username cannot be empty.")
            }
            if (username.length > 100) {
                return Pair(false, "Username cannot be more than 100 characters.")
            }
            if (username[0].isDigit()) {
                return Pair(false, "Username cannot start with a number.")
            }
            if (username.matches("^[a-zA-Z0-9 ]+$".toRegex()).not()) {
                return Pair(false, "Username can only contain alphabets and numbers.")
            }
            return Pair(true, "")
        }
        /**
         * Kiểm tra xem địa chỉ email đã cho có hợp lệ không.
         *
         * @param email Địa chỉ email để xác thực.
         * @return Một đối tượng [Pair] chứa giá trị boolean cho biết địa chỉ email có hợp lệ hay không và một chuỗi
         * thông báo mô tả kết quả xác nhận.
         */
        fun isEmailValid(email: String): Pair<Boolean, String> {
            if (email.isEmpty()) {
                return Pair(false, "Email cannot be empty.")
            }
            if (email[0].isDigit()) {
                return Pair(false, "Email cannot start with a number.")
            }
            if (EMAIL_ADDRESS_PATTERN.matcher(email).matches().not()) {
                return Pair(false, "Email is not valid.")
            }
            return Pair(true, "")
        }
        /**
         * Kiểm tra xem mật khẩu đã cho có hợp lệ không.
         *
         * @param pass Mật khẩu để xác thực.
         * @return Một đối tượng [Pair] chứa giá trị boolean cho biết mật khẩu có hợp lệ hay không và một chuỗi
         * thông báo mô tả kết quả xác nhận.
         */
        fun isPasswordValid(password: String): Pair<Boolean, String> {
            if (password.isEmpty()) {
                return Pair(false, "Password cannot be empty.")
            }
            if (PASSWORD_PATTERN.matcher(password).matches().not()) {
                return Pair(false, "Password is not valid.")
            }
            return Pair(true, "")
        }
    }
}