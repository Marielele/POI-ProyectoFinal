package com.carlele.groupnchat.models

class Publicacion(
    var id : String = "",
    var contenido : String = "",
    var autor : String = "",
    var hasImg : Boolean = false,
    var hasFile : Boolean = false,
    var grupo : String = ""
) {
}