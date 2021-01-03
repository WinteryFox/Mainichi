package app.mainichi.table

import java.time.LocalDate

data class PartialUser(
    val snowflake: Long,
    val username: String,
    val birthday: LocalDate?,
    val gender: Char?,
    val summary: String?,
    val avatar: String?
) {
    constructor(user: User) : this(
        user.snowflake,
        user.username,
        user.birthday,
        user.gender,
        user.summary,
        user.avatar
    )
}