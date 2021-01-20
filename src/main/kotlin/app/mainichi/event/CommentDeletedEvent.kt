package app.mainichi.event

import app.mainichi.table.Comment

class CommentDeletedEvent(
    comment: Comment
) : Event<Comment>(
    Type.COMMENT_DELETED,
    comment
)