package com.carlele.groupnchat.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.models.Publicacion
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class PublicacionesAdapter(private val listaPublicaciones: ArrayList<Publicacion>) : RecyclerView.Adapter<PublicacionesAdapter.PublicacionesViewHolder>() {

    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun OnItemClick(position: Int, contenido: String)
    }

    class PublicacionesViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val autor : TextView = itemView.findViewById(R.id.tv_autor)
        val contenido : TextView = itemView.findViewById(R.id.tv_publicacion)
        val autor_pic : CircleImageView = itemView.findViewById(R.id.img_autor_photo)
        val archivo : TextView = itemView.findViewById(R.id.tv_archivo)
        val img_publicacion : ImageView = itemView.findViewById(R.id.img_publicacion)

        init {
            itemView.setOnClickListener {
                listener.OnItemClick(adapterPosition, contenido.text.toString())
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicacionesViewHolder {
        val itemview =  LayoutInflater.from(parent.context).inflate(R.layout.layout_publicacion, parent, false)
        return PublicacionesViewHolder(itemview, mListener)
    }

    override fun onBindViewHolder(holder: PublicacionesViewHolder, position: Int) {
        val currentItem = listaPublicaciones[position]
        holder.autor.text = currentItem.autor
        holder.contenido.text = currentItem.contenido
        obtenerImagenUsuarioDB(currentItem.autor, holder.autor_pic)
        obtenerImagenPublicacionDB(currentItem.contenido.replace("\n", "").filter { !it.isWhitespace() }, holder.img_publicacion, currentItem.grupo)
        holder.archivo.text = "file"
    }

    override fun getItemCount(): Int {
        return listaPublicaciones.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    fun obtenerImagenUsuarioDB(nombre: String, img: CircleImageView){
        val storageRef = FirebaseStorage.getInstance().reference.child("Usuarios/$nombre")

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

    fun obtenerImagenPublicacionDB(text: String, img: ImageView, grupo:String){
        val storageRef = FirebaseStorage.getInstance().reference.child("Publicaciones/$grupo/$text/image")

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