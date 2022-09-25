package com.ssossotable.food

import io.socket.client.IO
import io.socket.client.Socket

object AppHelper {
    public val socket: Socket = IO.socket("*")
}