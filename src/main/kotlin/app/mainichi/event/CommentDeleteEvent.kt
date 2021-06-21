package app.mainichi.event

import app.mainichi.table.Comment

class CommentDeleteEvent(
    comment: Comment
) : Event<Comment>(
    Type.COMMENT_DELETE,
    comment
)