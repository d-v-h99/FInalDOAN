package com.hoangdoviet.finaldoan.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


private val sdf_HHmm = SimpleDateFormat("HH:mm", Locale.ROOT)
private val sdf_yyyyMMddHHmmss = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ROOT) // hoang sua
private val sdf_yyyyM = SimpleDateFormat("M-yyyy", Locale.ROOT) // dn tháng - năm
private val sdf_M = SimpleDateFormat("'Tháng' M", Locale("vi", "VN")) // tháng
private val sdf_yyyyMd = SimpleDateFormat("d 'tháng' M 'năm' yyyy", Locale("vi", "VN")) // ngày tháng năm
private val sdf_Md = SimpleDateFormat("d 'tháng' M", Locale("vi", "VN")) // ngày tháng

//nowMillis:
//Biến này là một getter được định nghĩa bằng cách sử dụng get().
//Nó sử dụng System.currentTimeMillis() để lấy thời gian hiện tại tính bằng mili giây.
//Điều này có nghĩa là mỗi lần truy cập nowMillis, giá trị trả về sẽ là thời gian hiện tại chính xác
// Tính linh hoạt: Việc sử dụng get() cho phép bạn truy cập giá trị thời gian hiện tại bất cứ lúc nào.
//Tính chính xác: Mỗi lần truy cập nowMillis, bạn sẽ nhận được giá trị thời gian hiện tại chính xác, phản ánh thời điểm thực tế bạn thực hiện truy vấn..
val nowMillis
    get() = System.currentTimeMillis() // các hăng số đại diện cho các đơn vị  : phút , giờ, ngày tính bằng milisecend
const val quarterMillis = 15 * 60 * 1000L //Nó đại diện cho số mili giây trong 15 phút. - Việc sử dụng L sau mỗi số cho biết giá trị là kiểu Long
const val hourMillis = 60 * 60 * 1000L //Nó đại diện cho số mili giây trong 1 giờ.
const val dayMillis = 24 * hourMillis //Nó đại diện cho số mili giây trong 1 ngày.
val Long.hours: Int
    get() = run {
        Calendar.getInstance().apply {
            time = Date(this@run)
        }.get(Calendar.HOUR_OF_DAY)
    }
val Long.dDays: Long //: Trả về số ngày khác biệt giữa thời gian hiện tại và thời điểm bắt đầu ngày (bỏ qua giờ, phút, giây).
    get() = (beginOfDay(this).timeInMillis - beginOfDay().timeInMillis) / dayMillis
//k có tham số truyền vào begin => mặc định nowMiliies
val Long.dMonths: Int //Trả về số tháng khác biệt giữa thời gian hiện tại và một thời điểm. Tính toán dựa trên sự chênh lệch năm và tháng.
    get() = (years - nowMillis.years) * 12 + monthOfYear - nowMillis.monthOfYear

val Long.years: Int // Trả về năm của thời gian hiện tại.
    get() = run {
        Calendar.getInstance().apply {
            time = Date(this@run)
        }.get(Calendar.YEAR)
    }
val Long.monthOfYear: Int // Trả về tháng (1-12) của thời gian hiện tại.
    get() = run {
        Calendar.getInstance().apply {
            time = Date(this@run)
        }.get(Calendar.MONTH) + 1
    }

val Long.calendar: Calendar //Chuyển đổi thời gian (mili giây) thành Calendar object.
    get() = Calendar.getInstance().apply { timeInMillis = this@calendar }

val Long.dayOfWeek: Int //Trả về thứ trong tuần (1 - Chủ nhật, 7 - Thứ Bảy).
    get() = calendar.get(Calendar.DAY_OF_WEEK)

val Long.dayOfMonth: Int //rả về ngày trong tháng (1-31).
    get() = calendar.get(Calendar.DAY_OF_MONTH)

val Long.dayOfYear: Int //Trả về ngày trong năm (1-366).
    get() = calendar.get(Calendar.DAY_OF_YEAR)

val Long.dayOfWeekText: String //rả về tên đầy đủ của thứ trong tuần (Ví dụ: "Chủ nhật").
    get() = run {
        when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Chủ nhật"
            Calendar.MONDAY -> "Thứ hai"
            Calendar.TUESDAY -> "Thứ ba"
            Calendar.WEDNESDAY -> "Thứ tư"
            Calendar.THURSDAY -> "Thứ năm"
            Calendar.FRIDAY -> "Thứ sáu"
            else -> "Thứ bảy"
        }
    }

val Long.dayOfWeekTextSimple: String // Trả về tên viết tắt của thứ trong tuần (Ví dụ: "CN").
    get() = run {
        when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "CN"
            Calendar.MONDAY -> "T2"
            Calendar.TUESDAY -> "T3"
            Calendar.WEDNESDAY -> "T4"
            Calendar.THURSDAY -> "T5"
            Calendar.FRIDAY -> "T6"
            else -> "T7"
        }
    }

val Long.HHmm: String //Định dạng thời gian theo giờ và phút (Ví dụ: "09:30").
    get() = sdf_HHmm.format(this)

val Long. yyyyMMddHHmmss: String // Định dạng thời gian theo năm, tháng, ngày, giờ, phút, giây (Ví dụ: "2024-05-17 16:51:42").
    get() = sdf_yyyyMMddHHmmss.format(this)

val Long.yyyyMd: String // Định dạng ngày tháng năm (Ví dụ: "2024年05月17日").
    get() = sdf_yyyyMd.format(this)

val Long.M: String //M: Định dạng tháng (Ví dụ: "5月").
    get() = sdf_M.format(this)

val Long.yyyyM: String //Định dạng tháng năm (Ví dụ: "2024-05").
    get() = sdf_yyyyM.format(this)

val Long.Md: String // Định dạng ngày tháng (Ví dụ: "05月17日").
    get() = sdf_Md.format(this)

val Calendar.HHmm: String
    get() = timeInMillis.HHmm

val Calendar.yyyyMMddHHmmss: String
    get() = timeInMillis.yyyyMMddHHmmss

val Calendar.yyyyMd: String
    get() = timeInMillis.yyyyMd

val Calendar.yyyyM: String
    get() = timeInMillis.yyyyM

val Calendar.M: String
    get() = timeInMillis.M

val Calendar.Md: String
    get() = timeInMillis.Md

val Calendar.firstDayOfMonthTime: Long // lay ngay dau tien cua thang
    get() = beginOfDay(timeInMillis).apply {
        set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
    }.timeInMillis
//Gọi beginOfDay để lấy đối tượng Calendar đại diện cho đầu ngày hiện tại (0 giờ, 0 phút, 0 giây).
//Áp dụng sửa đổi cho đối tượng Calendar:
//Đặt DAY_OF_MONTH thành giá trị tối thiểu bằng cách sử dụng getActualMinimum(Calendar.DAY_OF_MONTH). Điều này đảm bảo bạn lấy được ngày đầu tiên của tháng bất kể ngày hiện tại là gì.
//Trả về giá trị timeInMillis của đối tượng Calendar đã sửa đổi, đại diện cho dấu thời gian của ngày đầu tiên trong tháng.

val Calendar.lastDayOfMonthTime: Long
    get() = beginOfDay(timeInMillis).apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    }.timeInMillis
// lay ngay dau tien trong tuan
val Calendar.firstDayOfWeekTime: Long
    get() = beginOfDay(timeInMillis).apply {
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    }.timeInMillis
//Bắt đầu với beginOfDay để lấy đầu ngày hiện tại.
//Đặt DAY_OF_WEEK thành Calendar.SUNDAY để đặt rõ ràng ngày đầu tiên của tuần là Chủ nhật.
//Trả về giá trị timeInMillis của đối tượng Calendar đã sửa đổi, đại diện cho dấu thời gian của ngày đầu tiên tron

val Calendar.lastDayOfWeekTime: Long
    get() = beginOfDay(timeInMillis).apply {
        set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
    }.timeInMillis
// đầu vào là kiểu giá trị long (số nguyên dài) => đầu ra là kiểu giá trị date
fun beginOfDay(timestamp: Long = nowMillis): Calendar = Calendar.getInstance().apply {
    time = Date(timestamp) //Đặt time của đối tượng Calendar thành dấu thời gian được cung cấp bằng cách sử dụng Date(timestamp).
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    //ặt lại các thành phần thời gian (giờ, phút, giây, mili giây) thành 0 => 0h0p time đầu ngày đầu tiên
    //Trả về đối tượng Calendar đã tạo, đại diện cho đầu ngày cho dấu thời gian được cung cấp.
    //ví dụ
    //Current time: Fri May 17 16:30:45 GMT+00:00 2024
    //Begin of day: Fri May 17 00:00:00 GMT+00:00 2024
}
//Các hàm này điều chỉnh dấu thời gian hoặc khoảng thời gian dựa trên một khoảng thời gian cụ thể.
//roundTo làm tròn len ehay xuông, period : dấu time điều chỉnh ví dụ 15p , 1 ngày , 1 giờ
// true ấu thời gian sẽ được làm tròn lên hoặc xuống bội số gần nhất của period.
// Nếu false, dấu thời gian sẽ được điều chỉnh xuống bội số gần nhất của period trong quá khứ.

fun Long.adjustTimestamp(period: Long, roundTo: Boolean): Long {
    //Tính toán dấu thời gian vào đầu ngày bằng cách sử dụng beginOfDay(this).
    val zeroOfDay = beginOfDay(this).timeInMillis
    return if (!roundTo) {
        this - (this - zeroOfDay) % period
        //Tính toán phần dư khi sự khác biệt được chia cho chu kỳ.
        //Trừ phần dư khỏi dấu thời gian hiện tại để lấy dấu thời gian điều chỉnh rơi vào bội số gần nhất của chu kỳ trong quá khứ.
    } else { // neu true
        //ính toán một yếu tố bằng cách chia sự khác biệt cho chu kỳ.
        //Làm tròn yếu tố xuống số nguyên gần nhất bằng cách sử dụng roundToInt().
        //Nhân yếu tố được làm tròn với chu kỳ và cộng nó vào đầu ngày (zeroOfDay) để lấy dấu thời gian điều chỉnh rơi vào bội số gần nhất của chu kỳ (có thể trong tương lai hoặc quá khứ).
        (zeroOfDay + (1f * (this - zeroOfDay) / period).roundToInt() * period)
    }
    // vi du Fri May 17 10:35:00 GMT 2024
    // F thì 10:35 được điều chỉnh xuống 10:30. T thì ó vẫn là 10:30 vì thời gian ban đầu (10:35) gần hơn 10:30 so với 10:45.
}

fun Long.adjustDuration(period: Long, roundTo: Boolean): Long {
    return if (roundTo) {
        //Tính toán số chu kỳ bằng cách chia thời gian cho chu kỳ và làm tròn kết quả xuống số nguyên gần nhất.
        //Nhân số chu kỳ với chu kỳ để lấy thời gian điều chỉnh là bội
        (1f * this / period).roundToInt() * period
    } else {
        (1f * this / period).toInt() * period
    }
    // điều chỉnh phút gần nhất
}
