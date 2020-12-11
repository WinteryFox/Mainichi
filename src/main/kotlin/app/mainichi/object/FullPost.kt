package app.mainichi.`object`

import app.mainichi.table.Comment
import app.mainichi.table.Like
import app.mainichi.table.Post

class FullPost(
    snowflake: Long,
    author: Long,
    content: String,
    val likes: Array<Like>,
    val comments: Array<Comment>
) : Post(snowflake, author, content) {
    constructor(post: Post, likes: Array<Like>, comments: Array<Comment>) : this(
        post.snowflake,
        post.author,
        post.content,
        likes,
        comments
    )
}
