package com.carlele.groupnchat.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.models.Mensaje
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MensajesRecientesListaAdapter(private val listaChats: ArrayList<Mensaje>): RecyclerView.Adapter<MensajesRecientesListaAdapter.MensajesRecientesViewHolder>() {
    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun OnItemClick(position: Int, receptor:String)
    }

    class MensajesRecientesViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val name : TextView = itemView.findViewById(R.id.tv_nombre_contacto_usuario)
        val img : CircleImageView = itemView.findViewById(R.id.img_useruserimg)
        val con : TextView = itemView.findViewById(R.id.tv_mensaje_contenido_o)
        val time : TextView = itemView.findViewById(R.id.tv_tiempo_entrega)

        init {
            itemView.setOnClickListener {
                listener.OnItemClick(adapterPosition, name.text.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajesRecientesViewHolder {
        val itemview = LayoutInflater.from(parent.context).inflate(R.layout.layout_chats_recientes, parent, false)
        return MensajesRecientesViewHolder(itemview, mListener)
    }

    override fun onBindViewHolder(holder: MensajesRecientesViewHolder, position: Int) {
        val currentItem = listaChats[position]
        val user = Firebase.auth.currentUser
        val actualUser = user?.displayName.toString()

        if (currentItem.para == actualUser) {
            holder.name.text = currentItem.de
            obtenerImagenDB(holder.img, currentItem.de)

            if (currentItem.contenido.length > 30 ) {
                val cortar = currentItem.contenido.take(30)
                holder.con.text = "$cortar..."
            } else {
                holder.con.text = currentItem.contenido
            }

        } else {
            holder.name.text = currentItem.para
            obtenerImagenDB(holder.img, currentItem.para)

            if (currentItem.contenido.length > 30 ) {
                val cortar = currentItem.contenido.take(30)
                holder.con.text = "Tú: $cortar..."
            } else {
                holder.con.text = "Tú: ${currentItem.contenido}"
            }
        }

        if (currentItem.contenido == "") {
            if (currentItem.hasImage){
                holder.con.text = "Imagen"
            } else {
                holder.con.text = "Archivo"
            }
        }

        if (currentItem.encrypt) {
            holder.con.text = "Nuevo mensaje"
        }

        val dateFormater = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
        val fecha = dateFormater.format(Date(currentItem.timeStamp as Long))

        holder.time.text = fecha


    }

    override fun getItemCount(): Int {
        return listaChats.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    private fun obtenerImagenDB(img: CircleImageView, nombrechat : String){
        val storageRef = FirebaseStorage.getInstance().reference.child("Usuarios/$nombrechat")
        val storageRef2 = FirebaseStorage.getInstance().reference.child("Chats/$nombrechat")

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
                    storageRef2.getFile(localFileJPEG).addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeFile(localFileJPEG.absolutePath)
                        img.setImageBitmap(bitmap)
                    }.addOnFailureListener {
                        storageRef2.getFile(localFileJPG).addOnSuccessListener {
                            val bmp = BitmapFactory.decodeFile(localFileJPG.absolutePath)
                            img.setImageBitmap(bmp)
                        }.addOnFailureListener {
                            storageRef2.getFile(localFilePNG).addOnSuccessListener {
                                val bit = BitmapFactory.decodeFile(localFilePNG.absolutePath)
                                img.setImageBitmap(bit)
                            }
                        }
                    }
                }
            }
        }
    }

}