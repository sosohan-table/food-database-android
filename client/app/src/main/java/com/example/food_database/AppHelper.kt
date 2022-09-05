package com.example.food_database

import io.socket.client.IO
import io.socket.client.Socket

object Socket {
    public val socket: Socket = IO.socket("http://chat.socket.io")
}