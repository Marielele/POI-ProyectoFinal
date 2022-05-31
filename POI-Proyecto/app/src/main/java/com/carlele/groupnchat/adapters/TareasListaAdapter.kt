package com.carlele.groupnchat.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.models.NuevaTarea
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class TareasListaAdapter(private val listaTareas: ArrayList<NuevaTarea>): RecyclerView.Adapter<TareasListaAdapter.TareasViewHolder>() {

    class TareasViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val titulo : TextView = itemView.findViewById(R.id.tv_titulo_tarea)
        val descripcion : TextView = itemView.findViewById(R.id.tv_descripcion_tarea)
        val entregada : TextView = itemView.findViewById(R.id.tv_entregada)
        val btn_entregar : Button = itemView.findViewById(R.id.btn_entregar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareasViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_tareas, parent, false)
        return TareasViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TareasViewHolder, position: Int) {
        val currentItem = listaTareas[position]
        holder.titulo.text = currentItem.titulo
        holder.descripcion.text = currentItem.descripcion

        if (currentItem.entregada){
            holder.titulo.text = "${currentItem.titulo} - ${currentItem.grupo}"
            holder.btn_entregar.isVisible = false
            holder.entregada.isVisible = true
        }

        holder.btn_entregar.setOnClickListener { it2 ->

            val user = Firebase.auth.currentUser
            val actualUser = user?.displayName.toString()

            val tareaCompletada = NuevaTarea(currentItem.id, currentItem.autor, currentItem.titulo, currentItem.descripcion, currentItem.grupo, true)
            val reference = FirebaseDatabase.getInstance().getReference("TareasCompletadas").child(actualUser).child(currentItem.id)
            reference.setValue(tareaCompletada).addOnSuccessListener {
                Toast.makeText(it2.context, "Entregada!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun getItemCount(): Int {
        return listaTareas.size
    }
}