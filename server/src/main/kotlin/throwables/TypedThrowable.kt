package throwables

open class TypedThrowable(val type: String, message: String) : Throwable(message)