package com.example.food_database

import io.socket.client.IO
import io.socket.client.Socket

object AppHelper {
    public val socket: Socket = IO.socket("http://chat.socket.io")

}