package com.hoangdoviet.hoangfirebase.util

enum class Status {
    SUCCESS, // Indicates that a resource was successfully loaded.
    ERROR, // Indicates that there was an error while loading the resource.
    LOADING // Indicates that a resource is currently being loaded.
}
data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?
) {
    companion object{
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }
        fun <T> error(msg: String): Resource<T> {
            return Resource(Status.ERROR, null, msg)
        }
        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING, null, null)
        }
    }

}