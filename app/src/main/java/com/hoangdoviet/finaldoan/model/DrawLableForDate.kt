package com.hoangdoviet.finaldoan.model

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan


class DrawLableForDate(private val color: Int, private val value: String) : LineBackgroundSpan {
    // vẽ vb lên nhãn canvas
    override fun drawBackground(canvas: Canvas, paint: Paint,
                                left: Int, right: Int, top: Int,
                                baseline: Int, bottom: Int,
                                charSequence: CharSequence,
                                start: Int, end: Int, lineNum: Int) {
        val oldColor = paint.color // lưu trữ màu sắc hiên tại / kích thước chữ của paine
        val oldTextSize = paint.textSize
        if (color != 0) { // nếu màu sắc của nhãn k đc cung cấp => đặt màu sắc của paine là màu sắc của nhãn
            paint.setColor(color)
        }
        if (value != "") {
            paint.setTextSize(35f)
        }
        val text = value
        val x = right / 2f // Tính toán vị trí hoành độ (x) để canh giữa nhãn trong khoảng từ left đến right.
        val y = bottom * 1.25f //  toán vị trí tung độ (y) để đặt nhãn phía dưới dòng text, cách đáy một khoảng là 1.25 lần độ cao của dòng text.
        canvas.drawText(text, x, y, paint) // vẽ vb tại vị trí (x,y) với kiểu vẽ paint đã thiet lap truoc
        paint.textSize = oldTextSize
        paint.color = oldColor
    }
}
/*canvas: Đối tượng Canvas mà dòng văn bản sẽ được vẽ lên. Canvas là một bề mặt mà văn bản và các hình ảnh khác có thể được vẽ lên trong Android.
paint: Đối tượng Paint được sử dụng để vẽ văn bản. Nó chứa các thuộc tính như màu sắc, kiểu chữ, kích thước văn bản, vv.
left, right, top, baseline, bottom: Các giá trị này định vị vùng được sử dụng để vẽ dòng văn bản trên canvas.
left , righit toa độ của x => trục hoành Ox
top , bottom => trục Oy
baseline: Tọa độ y của baseline (đường cơ sở) của văn bản. Baseline là đường mà phần dưới của các ký tự được căn chỉnh theo
Đường baseline là đường vô hình where the bottom of most letters sits [đường mà đáy của hầu hết các chữ cái nằm]. Nó được sử dụng để căn chỉnh văn bản theo chiều dọc.
harSequence: Chuỗi ký tự mà dòng văn bản được vẽ từ đó.
start, end: Vị trí bắt đầu và kết thúc của phần của chuỗi ký tự được vẽ.
lineNum: Số thứ tự của dòng văn bản trong văn bản.
* */
/*Canvas được xem như là một bền mặt (hình dung như tờ giấy, bảng) mà chúng ta có thể vẽ bất cứ thứ gì lên đó. Ví dụ như vẽ một điểm, đường thằng, hình chữ nhật, đường tròn, elip, văn bản, hay thậm chí là một hình ảnh và các hình ảnh phức tạp khác nữa.
Các đối tượng trên Android như TextView, EditText, ImageView đều được vẽ trên canvas của hệ thống Android. Vậy vẽ các các đối tượng này như thế nào thì bài viết này sẽ cùng các bạn làm rõ.
* */