package app.mainichi.event

abstract class Event<T>(
    val type: Type,
    val data: T
) {
    enum class Type {
        POST_CREATE,
        LIKE_CREATE,
        LIKE_DELETE,
        COMMENT_CREATE,
        COMMENT_DELETE;
    }
}
