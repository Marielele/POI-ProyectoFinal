package com.carlele.groupnchat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.adapters.UsuariosListaAdapter
import com.carlele.groupnchat.models.Usuario
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class NuevoMensajeFragment : Fragment() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var usersRV: RecyclerView
    private lateinit var contactArray:ArrayList<Usuario>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_nuevo_mensaje, container, false)
        usersRV = view.findViewById(R.id.rv_lista_usuarios)

        contactArray = arrayListOf<Usuario>()
        obtenerUsuarios()
        return view
    }

    private fun obtenerUsuarios() {
        val user = Firebase.auth.currentUser
        val actualUser = user?.displayName
        dbRef = FirebaseDatabase.getInstance().getReference("Usuarios")
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(Usuario::class.java)
                        if (user!!.nombre != actualUser) {
                            contactArray.add(user!!)
                        }
                    }
                    var adatadorsito = UsuariosListaAdapter(contactArray)
                    usersRV.adapter = adatadorsito
                    adatadorsito.setOnItemClickListener(object : UsuariosListaAdapter.OnItemClickListener{
                        override fun OnItemClick(position: Int, nombre: String, con: String) {
                            Toast.makeText(context, "click en $position, $nombre, $con", Toast.LENGTH_SHORT).show()
                            abrirFragmentDatos(ChatFragment(), nombre)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun abrirFragmentDatos(fragmento: Fragment, dato: String){
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        val myBundle = Bundle()
        myBundle.putString("receptor", dato)
        fragmento.arguments = myBundle
        transaction.replace(R.id.container, fragmento).commit()
    }

}