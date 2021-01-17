package app.mainichi.table

import java.time.LocalDate

data class PartialUser(
    val id: Long,
    val username: String,
    val birthday: LocalDate?,
    val gender: Char?,
    val summary: String?,
    val avatar: String?,
    val version: Long
) {
    constructor(user: User) : this(
        user.id,
        user.username,
        user.birthday,
        user.gender,
        user.summary,
        user.avatar,
        user.version
    )
}