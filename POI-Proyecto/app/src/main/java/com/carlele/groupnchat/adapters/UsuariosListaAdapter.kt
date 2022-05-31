package com.carlele.groupnchat.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.models.Usuario
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class UsuariosListaAdapter(private val listaUsuarios: ArrayList<Usuario>) : RecyclerView.Adapter<UsuariosListaAdapter.UsuariosListaViewHolder>() {
    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun OnItemClick(position: Int, nombre:String, con:String)
    }

    class UsuariosListaViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val name : TextView = itemView.findViewById(R.id.tv_username)
        val img : CircleImageView = itemView.findViewById(R.id.img_mini_user)
        val con : TextView = itemView.findViewById(R.id.tv_con)
        init {
            itemView.setOnClickListener {
                listener.OnItemClick(adapterPosition, name.text.toString(), con.text.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuariosListaViewHolder {
        val itemview = LayoutInflater.from(parent.context).inflate(R.layout.layout_usuario_y_foto, parent, false)
        return UsuariosListaViewHolder(itemview, mListener)
    }

    override fun onBindViewHolder(holder: UsuariosListaViewHolder, position: Int) {
        val currentItem = listaUsuarios[position]
        holder.name.text = currentItem.nombre
        if (currentItem.estado == "offline") {
            holder.con.setTextColor(Color.GRAY)
            holder.con.getCompoundDrawables()[0].setTint(Color.GRAY)
        }
        holder.con.text = currentItem.estado
        obtenerImagenDB(currentItem.nombre, holder.img)
    }

    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    fun obtenerImagenDB(username: String, img: CircleImageView){
        val storageRef = FirebaseStorage.getInstance().reference.child("Usuarios/$username")

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