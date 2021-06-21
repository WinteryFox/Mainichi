package app.mainichi.event

import app.mainichi.table.Comment

class CommentCreateEvent(
    comment: Comment
) : Event<Comment>(
    Type.COMMENT_CREATE,
    comment
)