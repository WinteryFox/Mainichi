package app.mainichi.`object`

import app.mainichi.table.Comment

class FullPost(
    val snowflake: Long,
    val author: Long,
    val content: String,
    val likers: Array<Long>,
    private val commentSnowflakes: Array<Long>,
    private val commentPosts: Array<Long>,
    private val commentCommenters: Array<Long>,
    private val commentContents: Array<String>
) {
    val comments: Array<Comment> = commentSnowflakes.mapIndexed { index, s ->
        Comment(
            s,
            commentPosts[index],
            commentCommenters[index],
            commentContents[index]
        )
    }.toTypedArray()
}
