package app.mainichi.event

import app.mainichi.`object`.ShortPost

class PostCreateEvent(
    post: ShortPost
) : Event<ShortPost>(Type.POST_CREATE, post)
