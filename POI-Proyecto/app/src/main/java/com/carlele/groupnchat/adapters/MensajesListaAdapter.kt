package com.carlele.groupnchat.adapters

import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.models.Mensaje
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MensajesListaAdapter(private val listaMensajes: MutableList<Mensaje>) : RecyclerView.Adapter<MensajesListaAdapter.MensajesListaViewHolder>() {
    class MensajesListaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun asignarInfo(mensaje: Mensaje){
            val contenido : TextView = itemView.findViewById(R.id.tv_contenido)
            val hora : TextView = itemView.findViewById(R.id.tv_msg_time)
            val cajaMensaje : LinearLayout = itemView.findViewById(R.id.caja_mensaje)
            val imagen : ImageView = itemView.findViewById(R.id.img_mensaje)
            val archivo : TextView = itemView.findViewById(R.id.tv_file)
            val idsito = "${mensaje.de} y ${mensaje.para}".replace("\n", "").filter { !it.isWhitespace() }

            if (mensaje.contenido == ""){
                contenido.isVisible = false
            } else {
                contenido.text = mensaje.contenido
            }

            if (mensaje.hasFile) {
                archivo.text = "Archivo"
                archivo.isVisible = true
            }

            if (mensaje.hasImage) {
                obtenerImagenDB(imagen, idsito, mensaje.id)
                imagen.isVisible = true
            }

            val dateFormater = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
            val fecha = dateFormater.format(Date(mensaje.timeStamp as Long))

            hora.text = fecha

            val params = cajaMensaje.layoutParams

            if (mensaje.esMio) {
                val newParams = FrameLayout.LayoutParams(
                    params.width,
                    params.height,
                    Gravity.END
                )
                cajaMensaje.layoutParams = newParams
            } else {
                val newParams = FrameLayout.LayoutParams(
                    params.width,
                    params.height,
                    Gravity.START
                )
                cajaMensaje.layoutParams = newParams
            }

        }

        private fun obtenerImagenDB(img: ImageView, nombrechat : String, nombreimg: String){
            val storageRef = FirebaseStorage.getInstance().reference.child("Chats/$nombrechat/$nombreimg")

            val localFileJPG = File.createTempFile("tempImg", "jpg")
            val localFilePNG = File.createTempFile("tempImg", "png")
            val localFileJPEG = File.createTempFile("tempImg", "jpeg")

            storageRef.getFile(localFileJPEG).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFileJPEG.absolutePath)
                img.setImageBitmap(bitmap)
            }.addOnFailureListener {
                storageRef.getFile(localFileJPG).addOnSuccessListener {
                    val bmp = BitmapFactory.decodeFile(localFileJPG.absolutePath)
                    img.setImageBitmap(bmp)
                }.addOnFailureListener {
                    storageRef.getFile(localFilePNG).addOnSuccessListener {
                        val bit = BitmapFactory.decodeFile(localFilePNG.absolutePath)
                        img.setImageBitmap(bit)
                    }.addOnFailureListener {

                    }
                }
            }
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajesListaViewHolder {
        val miChatView = LayoutInflater.from(parent.context).inflate(R.layout.layout_mensaje, parent, false)
        return MensajesListaViewHolder(miChatView)
    }

    override fun onBindViewHolder(holder: MensajesListaViewHolder, position: Int) {
        holder.asignarInfo(listaMensajes[position])
    }

    override fun getItemCount(): Int {
        return listaMensajes.size
    }


}