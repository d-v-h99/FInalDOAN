package com.hoangdoviet.finaldoan.utils

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import java.io.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
//Delegate thực chất là một class cấp giá trị cho thuộc tính và xử lý sự thay đổi của nó. Điều này cho phép chúng ta di chuyển, ủy thác , getter – setter logic từ thuộc tính của nó sang 1 class riêng biệt và tái sử dụng lại logic này.
fun <T : Any> argument(): ReadWriteProperty<Fragment, T> =
    FragmentArgumentDelegate()

fun <T : Any> argumentNullable(): ReadWriteProperty<Fragment, T?> =
    FragmentNullableArgumentDelegate()
//FragmentArgumentDelegate là một delegate được sử dụng để truy cập các đối số (arguments) của Fragment trong Android.
//Khi bạn khai báo một thuộc tính trong Fragment và muốn truy cập đối số tương ứng với nó từ Bundle, bạn có thể sử dụng FragmentArgumentDelegate.
//Delegate này sẽ đảm bảo rằng việc truy cập đối số sẽ được thực hiện một cách an toàn và thuận tiện.
//Cách hoạt động:
//
//Khi thuộc tính được truy cập, delegate sẽ sử dụng tên của thuộc tính đó để truy xuất giá trị tương ứng từ Bundle của Fragment.
//Nếu không tìm thấy giá trị, hoặc giá trị là null, delegate sẽ ném ra một IllegalStateException.
//Khi thuộc tính được gán một giá trị, delegate sẽ lưu giá trị đó vào Bundle của Fragment với key là tên của thuộc tính.
//Việc sử dụng delegate này giúp giảm bớt mã lặp lại và làm cho code trở nên dễ đọc hơn bằng cách ẩn đi các phương thức gọi getArguments() và putArguments() bên trong implementation của delegate.
private class FragmentArgumentDelegate<T : Any>
    : ReadWriteProperty<Fragment, T> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(
        thisRef: Fragment,
        property: KProperty<*>
    ): T {
        val key = property.name
        return thisRef.arguments
            ?.get(key) as? T
            ?: throw IllegalStateException("Property ${property.name} could not be read")
    }

    override fun setValue(
        thisRef: Fragment,
        property: KProperty<*>, value: T
    ) {
        val args = thisRef.arguments
            ?: Bundle().also(thisRef::setArguments)
        val key = property.name
        args.put(key, value)
    }
}
//FragmentNullableArgumentDelegate là một delegate tương tự như FragmentArgumentDelegate, nhưng được sử dụng khi thuộc tính có thể có giá trị null.
//Delegate này cho phép truy cập đối số mà có thể là null một cách an toàn.

//Khi thuộc tính được truy cập, delegate sẽ sử dụng tên của thuộc tính đó để truy xuất giá trị tương ứng từ Bundle của Fragment.
//Nếu không tìm thấy giá trị, hoặc giá trị là null, delegate sẽ trả về null mà không ném ra bất kỳ exception nào.
//Khi thuộc tính được gán một giá trị, delegate sẽ lưu giá trị đó vào Bundle của Fragment với key là tên của thuộc tính. Nếu giá trị là null, delegate sẽ loại bỏ key tương ứng từ Bundle.
private class FragmentNullableArgumentDelegate<T : Any?> :
    ReadWriteProperty<Fragment, T?> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(
        thisRef: Fragment,
        property: KProperty<*>
    ): T? {
        val key = property.name
        return thisRef.arguments?.get(key) as? T
    }

    override fun setValue(
        thisRef: Fragment,
        property: KProperty<*>, value: T?
    ) {
        val args = thisRef.arguments
            ?: Bundle().also(thisRef::setArguments)
        val key = property.name
        value?.let { args.put(key, it) } ?: args.remove(key)
    }
}

private fun <T> Bundle.put(key: String, value: T) {
    when (value) {
        is String -> putString(key, value)
        is Int -> putInt(key, value)
        is Short -> putShort(key, value)
        is Long -> putLong(key, value)
        is Byte -> putByte(key, value)
        is ByteArray -> putByteArray(key, value)
        is Char -> putChar(key, value)
        is CharArray -> putCharArray(key, value)
        is CharSequence -> putCharSequence(key, value)
        is Float -> putFloat(key, value)
        is Bundle -> putBundle(key, value)
        is Parcelable -> putParcelable(key, value)
        is Serializable -> putSerializable(key, value)
        else -> throw IllegalStateException("invalid value: $value")
    }
}
