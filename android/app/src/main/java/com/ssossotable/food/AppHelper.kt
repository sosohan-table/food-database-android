package com.ssossotable.food

import io.socket.client.IO
import io.socket.client.Socket

object AppHelper {
    public val socket: Socket = IO.socket("http://18.117.84.165:3000")
}