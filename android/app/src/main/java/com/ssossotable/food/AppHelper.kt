package com.ssossotable.food

import io.socket.client.IO
import io.socket.client.Socket

object AppHelper {
    public val socket: Socket = IO.socket("http://192.168.45.39:3000")
    //http://18.116.71.97
}