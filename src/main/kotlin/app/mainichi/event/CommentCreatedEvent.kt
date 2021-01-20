package app.mainichi.event

import app.mainichi.table.Comment

class CommentCreatedEvent(
    comment: Comment
) : Event<Comment>(
    Type.COMMENT_CREATED,
    comment
)