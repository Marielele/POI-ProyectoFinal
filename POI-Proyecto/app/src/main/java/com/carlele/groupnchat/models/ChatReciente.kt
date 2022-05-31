package com.carlele.groupnchat.models

import com.google.firebase.database.Exclude

class ChatReciente(
    var id: String = "",
    var de: String = "",
    var para: String = "",
    var contenido: String = "",
    var tieneImg: Boolean = false,
    var tieneFile: Boolean = false,
    val timeStamp: Any? = null
) {
    @Exclude
    var esMio: Boolean = false
}