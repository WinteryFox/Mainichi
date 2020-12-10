package app.mainichi.session

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("sessions")
data class Session(
    @Id
    val id: UUID,
    @Suppress("ArrayInDataClass")
    val attributes: ByteArray,
    @Column("max_idle_time")
    val maxIdleTime: Long,
    @Column("creation_time")
    val creationTime: Instant,
    @Column("last_access_time")
    val lastAccessTime: Instant,
    @Column("valid")
    val isValid: Boolean,
    @Version
    val version: Long
)
