package app.mainichi.event

abstract class Event<T>(
    val type: Type,
    val data: T
) {
    enum class Type {
        POST_CREATED,
        LIKE_CREATED,
        LIKE_DELETED;
    }
}
