package com.carlele.groupnchat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.models.Subgrupo

class SubgruposListaAdapter(private val listaSubgrupos: ArrayList<Subgrupo>): RecyclerView.Adapter<SubgruposListaAdapter.SubgruposViewHolder>() {
    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun OnItemClick(position: Int, nombre: String)
    }

    class SubgruposViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView){
        val sub_name = itemView.findViewById<TextView>(R.id.tv_titulo_sub)

        init {
            itemView.setOnClickListener {
                listener.OnItemClick(adapterPosition, sub_name.text.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubgruposViewHolder {
        val itemview = LayoutInflater.from(parent.context).inflate(R.layout.layout_subgrupo, parent, false)
        return SubgruposViewHolder(itemview, mListener)
    }

    override fun onBindViewHolder(holder: SubgruposViewHolder, position: Int) {
        val currentItem = listaSubgrupos[position]
        holder.sub_name.text = currentItem.nombre
    }

    override fun getItemCount(): Int {
        return listaSubgrupos.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

}