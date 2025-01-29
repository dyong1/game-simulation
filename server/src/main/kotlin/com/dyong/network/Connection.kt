package com.dyong.network

import java.io.IOException
import java.net.Socket

class Connection(
    private var socket: Socket
) {
    private var writer = socket.getOutputStream().bufferedWriter()
    private var reader = socket.getInputStream().bufferedReader()

    fun readMessage(): String? {
        return try {
            this.reader.readLine()
        } catch (e: IOException) {
            null
        }
    }

    fun sendMessage(message: String) {
        this.writer.write(message)
        this.writer.flush()
    }

    fun close() {
        socket.close()
    }
}