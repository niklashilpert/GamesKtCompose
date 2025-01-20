package shared.connection

import java.io.Closeable

class Player(private val connection: PacketConnection, val name: String) : Closeable {
    val isClosed get() = connection.isClosed

    fun read(): InetPacket {
        return connection.read()
    }

    fun write(packet: InetPacket) {
        connection.write(packet)
    }

    fun respond(responseCode: ResponseCode) {
        connection.respond(responseCode)
    }

    override fun close() {
        connection.close()
    }
}

