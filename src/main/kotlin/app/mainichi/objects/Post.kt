package app.mainichi.objects

data class Post(
        val snowflake: Long,
        val author: Long,
        val content: String
)