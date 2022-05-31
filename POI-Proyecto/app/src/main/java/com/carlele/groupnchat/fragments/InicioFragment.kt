package com.carlele.groupnchat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.adapters.RecompensasAdapter
import com.carlele.groupnchat.models.Logros
import com.carlele.groupnchat.models.NuevaTarea
import com.carlele.groupnchat.models.Rewards
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class InicioFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var gridLayoutManager: GridLayoutManager ? = null
    private var arrayList: ArrayList<Rewards> ? = null
    private var rewardsAdapter: RecompensasAdapter ? = null
    private lateinit var database : DatabaseReference
    private var usuario: String = ""
    private lateinit var logrosArray: ArrayList<Logros>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_inicio, container, false)

        val user = Firebase.auth.currentUser
        val actualUser = user?.displayName
        usuario = actualUser.toString()

        logrosArray = arrayListOf<Logros>()

        recyclerView = view.findViewById(R.id.rv_rewards)
        gridLayoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false)
        recyclerView?.layoutManager = gridLayoutManager
        recyclerView?.setHasFixedSize(true)
        arrayList = ArrayList()
        arrayList = agregarData()
        rewardsAdapter = RecompensasAdapter(view.context, arrayList!!)
        recyclerView?.adapter = rewardsAdapter

        return view
    }

    private fun CambiarEntreFragments(fm: Fragment){
        val transactio: FragmentTransaction = parentFragmentManager.beginTransaction()
        transactio.replace(R.id.container, fm).commit()
    }

    private fun agregarData(): ArrayList<Rewards>{
        var items: ArrayList<Rewards> = ArrayList()
        items.add(Rewards(R.drawable.unicorn_hi, "¡Bienvenido!", "Crea una cuenta"))
        items.add(Rewards(R.drawable.unicorn_like, "Comunicate", "Envia un mensaje"))
        items.add(Rewards(R.drawable.unicorn_dance, "Agrupando", "Crea un chat grupal"))
        items.add(Rewards(R.drawable.unicorn_stick, "¡Mira esto!", "Envia una imagen"))
        items.add(Rewards(R.drawable.unicorn_glasses, "Responsable", "Completa una tarea"))
        items.add(Rewards(R.drawable.unicorn_refinated, "Socializando", "Entra a un chat grupal"))
        items.add(Rewards(R.drawable.unicorn_stars, "Publicate", "Crea una publicacion"))
        items.add(Rewards(R.drawable.unicorn_cake, "Parte de", "Crea un subgrupo"))
        items.add(Rewards(R.drawable.unicorn_buag, "Aguafiestas", "Crea una tarea"))
        return items
    }


}