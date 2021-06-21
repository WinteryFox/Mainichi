package app.mainichi.event

import app.mainichi.table.Like

class LikeDeleteEvent(
    like: Like
) : Event<Like>(
    Type.LIKE_DELETE,
    like
)