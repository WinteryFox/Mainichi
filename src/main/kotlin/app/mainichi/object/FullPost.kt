package app.mainichi.`object`

import app.mainichi.table.Post

class FullPost(
    snowflake: Long,
    author: Long,
    content: String,
    val likes: Set<Long>
) : Post(snowflake, author, content)
