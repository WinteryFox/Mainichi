package app.mainichi.`object`

import app.mainichi.table.Post

class ShortPost(
    snowflake: Long,
    author: Long,
    content: String,
    val likeCount: Int,
    val commentCount: Int
) : Post(snowflake, author, content)