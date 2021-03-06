package app.mainichi.service

import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.StringBuilder
import java.net.NetworkInterface
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

private const val UNUSED_BITS = 1
private const val EPOCH_BITS = 41
private const val NODE_BITS = 10
private const val SEQUENCE_BITS = 12

private const val MAX_NODE_ID: Long = (1L shl NODE_BITS) - 1
private const val MAX_SEQUENCE: Long = (1L shl SEQUENCE_BITS) - 1

@Service
class SnowflakeService(
    private val start: Long = Instant.parse("2021-01-01T00:00:00.00Z").toEpochMilli(),
    private val nodeId: Long = generateNodeId()
) {
    private var sequence = AtomicLong(0L)
    private var last = AtomicLong(timestamp())

    init {
        if (start < 0)
            throw IllegalArgumentException("Start time may not be less than 0")
        if (start > Instant.now().toEpochMilli())
            throw IllegalArgumentException("Start time may not be in the future")
    }

    fun next(): Long {
        var now = timestamp()

        if (now < last.get())
            throw IllegalStateException("The time moved backwards!")

        if (now == last.get())
            sequence.set((sequence.incrementAndGet() + 1) and MAX_SEQUENCE)
            if (sequence.get() == 0L)
                while (now == last.get())
                    now = timestamp()
        else
            sequence.set(0)

        last.set(now)

        return now shl
                (NODE_BITS + SEQUENCE_BITS) or
                (nodeId shl SEQUENCE_BITS) or
                sequence.get()
    }

    private fun timestamp() = Instant.now().toEpochMilli() - start

    companion object {
        private fun generateNodeId(): Long {
            val builder = StringBuilder()
            val interfaces = NetworkInterface.getNetworkInterfaces()

            while (interfaces.hasMoreElements()) {
                val i = interfaces.nextElement()
                val mac = i.hardwareAddress

                if (mac != null)
                    for (port in mac)
                        builder.append(String.format("%02X", port))
            }

            return builder.toString().hashCode().toLong() and MAX_NODE_ID
        }
    }
}