package com.carlele.groupnchat.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.models.Grupo
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class GruposListaAdapter(private val listaGrupos: ArrayList<Grupo>) : RecyclerView.Adapter<GruposListaAdapter.GruposListaViewHolder>() {
    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun OnItemClick(position: Int, nombre: String)
    }

    class GruposListaViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val nombre : TextView = itemView.findViewById(R.id.txt_nombre_de_grupo)
        val img : ImageView = itemView.findViewById(R.id.img_grupo)

        init {
            itemView.setOnClickListener {
                listener.OnItemClick(adapterPosition, nombre.text.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GruposListaViewHolder {
        val itemview = LayoutInflater.from(parent.context).inflate(R.layout.layout_tarjeta_grupo, parent, false)
        return GruposListaViewHolder(itemview, mListener)
    }

    override fun onBindViewHolder(holder: GruposListaViewHolder, position: Int) {
        val currentItem = listaGrupos[position]
        holder.nombre.text = currentItem.nombre
        obtenerImagenDB(currentItem.nombre, holder.img)
    }

    override fun getItemCount(): Int {
        return listaGrupos.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    fun obtenerImagenDB(nombre: String, img: ImageView){
        val storageRef = FirebaseStorage.getInstance().reference.child("Grupos/$nombre")

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