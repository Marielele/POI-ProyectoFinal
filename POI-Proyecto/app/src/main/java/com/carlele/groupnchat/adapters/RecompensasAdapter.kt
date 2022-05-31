package com.carlele.groupnchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.models.Rewards

class RecompensasAdapter(var context: Context, var arrayList: ArrayList<Rewards>) : RecyclerView.Adapter<RecompensasAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context).inflate(R.layout.layout_tarjeta_grupo, parent, false)
        return ItemHolder(itemHolder)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        var rewards:Rewards = arrayList.get(position)
        holder.icon.setImageResource(rewards.iconReward!!)
        holder.reward.text = rewards.nameReward

        holder.reward.setOnClickListener {
            Toast.makeText(context, rewards.descriptionR, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var icon = itemView.findViewById<ImageView>(R.id.img_grupo)
        var reward = itemView.findViewById<TextView>(R.id.txt_nombre_de_grupo)
    }

}