package com.carlele.groupnchat.models

class Subgrupo(
    var id: String = "",
    var nombre: String = "",
    var integrantes: MutableList<String> = mutableListOf(),
    var carrera: String = "",
    var grupo: String = ""
) {
}