package com.carlele.groupnchat.models

import com.google.firebase.database.Exclude

class Mensaje(
    var id: String = "",
    var contenido: String = "",
    var de: String = "",
    val timeStamp: Any? = null,
    var para: String = "",
    var encrypt : Boolean = false,
    var hasImage : Boolean = false,
    var hasFile : Boolean = false,
    var isGroup : Boolean = false
)  {
    @Exclude
    var esMio: Boolean = false
}
