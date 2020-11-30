package app.mainichi.data

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import javax.persistence.PostLoad
import javax.persistence.PostPersist

val snowflakeGenerator = Snowflake(Snowflake.createNodeId(), 1577836800000L)

abstract class SnowflakePersistable(givenId: Long? = null) : Persistable<Long> {
    @Id
    private val snowflake = givenId ?: snowflakeGenerator.nextId()

    @Transient
    private var persisted = givenId != null

    override fun getId(): Long? = snowflake

    override fun isNew(): Boolean = !persisted

    override fun hashCode(): Int = snowflake.hashCode()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is SnowflakePersistable -> false
            else -> id == other.id
        }
    }

    @PostPersist
    @PostLoad
    private fun setPersisted() {
        persisted = true
    }
}