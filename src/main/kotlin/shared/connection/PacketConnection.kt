package shared.connection

import java.io.Closeable
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class PacketConnection(private val socket: Socket, isClientSide: Boolean) : Closeable {
    private val dataIn: ObjectInputStream
    private val dataOut: ObjectOutputStream

    init {
        try {
            if (isClientSide) {
                dataOut = ObjectOutputStream(socket.getOutputStream())
                dataIn = ObjectInputStream(socket.getInputStream())
            } else {
                dataIn = ObjectInputStream(socket.getInputStream())
                dataOut = ObjectOutputStream(socket.getOutputStream())
            }
        } catch (e: IOException) {
            throw IOException("Error while initializing packet connection", e)
        }
    }

    private val readLock = Any()
    private val sendLock = Any()
    var isClosed = false
        private set

    /**
     * Reads an object from the connection's input stream.
     * @return The DataPacket that was read from the input stream.
     * @throws IOException If an exception occurred while trying to read data from the input stream.
     * @throws ClassNotFoundException If the read data is not of type DataPacket.
     */
    fun read(): InetPacket {
        synchronized(readLock) {
            if (!isClosed) {
                return dataIn.readObject() as InetPacket
            } else {
                throw IOException("Connection closed")
            }
        }
    }

    /**
     * Writes a DataPacket to the connection's output stream.
     * @throws IOException If an exception occurred while trying to write the packet.
     */
    fun write(packet: InetPacket) {
        synchronized(sendLock) {
            if (!isClosed) {
                dataOut.writeObject(packet)
                dataOut.reset()
            } else {
                throw IOException("Connection is closed")
            }
        }
    }

    /**
     * Writes a Result data packet with the specified result code to the connection's output stream.
     * @throws IOException If an exception occurred while trying to write the packet.
     */
    fun respond(responseCode: ResponseCode) {
        write(InetPacket.Response(responseCode))
    }

    override fun close() {
        if (!isClosed) {
            isClosed = true
            dataOut.close()
            dataIn.close()
            socket.close()
        }
    }
}