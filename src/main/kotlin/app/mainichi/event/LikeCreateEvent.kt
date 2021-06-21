package app.mainichi.event

import app.mainichi.table.Like

class LikeCreateEvent(
    like: Like
) : Event<Like>(
    Type.LIKE_CREATE,
    like
)
