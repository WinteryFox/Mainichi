package app.mainichi.data

import java.lang.IllegalStateException
import java.net.NetworkInterface
import java.security.SecureRandom

class Snowflake(
    private var nodeId: Long,
    private var customEpoch: Long
) {
    companion object {
        private const val NODE_ID_BITS = 10
        private const val SEQUENCE_BITS = 12
        private const val maxNodeId = (1L shl NODE_ID_BITS) - 1

        fun createNodeId(): Long {
            val nodeId = try {
                val sb = StringBuilder()
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()

                while (networkInterfaces.hasMoreElements()) {
                    val networkInterface = networkInterfaces.nextElement()
                    val mac = networkInterface.hardwareAddress

                    for (macPort in mac)
                        sb.append(String.format("%02X", macPort))
                }
                sb.toString().hashCode().toLong()
            } catch (e: Exception) {
                SecureRandom().nextLong()
            }
            return nodeId and maxNodeId
        }
    }

    private val maxSequence = (1L shl SEQUENCE_BITS) - 1
    private var lastTimestamp = -1L
    private var sequence = 0L

    init {
        if (nodeId < 0 || nodeId > maxNodeId)
            throw IllegalArgumentException(String.format("NodeId must be between %d and %d", 0, maxNodeId))
    }

    @Synchronized
    fun nextId(): Long {
        var currentTimestamp = System.currentTimeMillis()

        if (currentTimestamp < lastTimestamp)
            throw IllegalStateException("Invalid System Clock!")

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) and maxSequence
            if (sequence == 0L)
                currentTimestamp = waitNextMillis(currentTimestamp)
        } else
            sequence = 0

        lastTimestamp = currentTimestamp

        return currentTimestamp.shl(NODE_ID_BITS + SEQUENCE_BITS).or(nodeId shl SEQUENCE_BITS).or(sequence)
    }

    //Get current timestamp in millis, adjust for the custom epoch
    private fun timestamp() = System.currentTimeMillis() - customEpoch

    private fun waitNextMillis(currentTimeStamp: Long): Long {
        var stamp = currentTimeStamp

        while (stamp == lastTimestamp)
            stamp = timestamp()

        return stamp
    }

    fun parse(id: Long): LongArray {
        val maskNodeId: Long = ((1L shl NODE_ID_BITS) - 1) shl SEQUENCE_BITS
        val maskSequence: Long = (1L shl SEQUENCE_BITS) - 1

        val timestamp: Long = (id shr (NODE_ID_BITS + SEQUENCE_BITS)) + customEpoch
        val nodeId: Long = (id and maskNodeId) shr SEQUENCE_BITS
        val sequence: Long = id and maskSequence
        return longArrayOf(timestamp, nodeId, sequence)
    }
}