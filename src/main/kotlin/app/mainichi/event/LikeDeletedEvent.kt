package app.mainichi.event

import app.mainichi.table.Like

class LikeDeletedEvent(
    like: Like
) : Event<Like>(
    Type.LIKE_DELETED,
    like
)