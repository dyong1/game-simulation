package com.dyong.network

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer
import java.net.Socket

class Connection {
    private var socket: Socket
    private var writer: Writer
    private var reader: BufferedReader

    constructor(socket: Socket) {
        this.socket = socket
        this.writer = OutputStreamWriter(socket.getOutputStream())
        this.reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    }
    fun readMessage(): String? {
        return this.reader.readLine()
    }
    fun sendMessage(message: String) {
        this.writer.write(message)
        this.writer.flush()
    }
    fun isConnected(): Boolean {
        return socket.isConnected
    }
}