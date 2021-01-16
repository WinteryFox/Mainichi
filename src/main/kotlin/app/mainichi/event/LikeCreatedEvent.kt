package app.mainichi.event

import app.mainichi.table.Like

class LikeCreatedEvent(
    like: Like
) : Event<Like>(
    Type.LIKE_CREATED,
    like
)
