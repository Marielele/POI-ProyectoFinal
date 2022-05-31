package com.carlele.groupnchat.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.adapters.TareasListaAdapter
import com.carlele.groupnchat.models.NuevaTarea
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class TareasFragment : Fragment() {

    private var db = FirebaseDatabase.getInstance()
    private var tareasRef = db.getReference("Tareas")
    var usuario: String = ""
    private lateinit var database : DatabaseReference
    private lateinit var tareasRV: RecyclerView
    private lateinit var tareasArray: ArrayList<NuevaTarea>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tareas, container, false)
        val btn_crear = view.findViewById<Button>(R.id.btn_crea_tarea)
        val recycler = view.findViewById<RecyclerView>(R.id.rv_homework)

        val user = Firebase.auth.currentUser
        val actualUser = user?.displayName
        usuario = actualUser.toString()

        tareasRV = recycler
        tareasArray = arrayListOf<NuevaTarea>()
        CargarTareas()

        btn_crear.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            val vista = layoutInflater.inflate(R.layout.layout_dialog_crear_tarea, null)
            builder.setView(vista)
            val dialog = builder.create()
            dialog.show()

            val titulo = vista.findViewById<EditText>(R.id.txt_titulo_tarea)
            val descripcion = vista.findViewById<EditText>(R.id.txt_descripcion_tarea)
            val crear = vista.findViewById<Button>(R.id.btn_crear_tarea)
            val grupo = vista.findViewById<EditText>(R.id.txt_tarea_grupo)

            crear.setOnClickListener {
                if (titulo.text.isEmpty() || descripcion.text.isEmpty() || grupo.text.isEmpty()) {
                    Toast.makeText(view.context, "Para hacer una tarea escriba un titulo, una descripcion y el grupo a quien va la tarea", Toast.LENGTH_SHORT).show()
                } else {
                    val data = db.getReference("Grupos").child(grupo.text.toString()).get()
                    data.addOnSuccessListener {
                        if (it.exists()) {
                            val idsito = titulo.text.toString().replace("\n", "").filter { !it.isWhitespace() }
                            crearTarea(NuevaTarea(idsito, usuario, titulo.text.toString(), descripcion.text.toString(), grupo.text.toString()))
                            Toast.makeText(view.context, "Tarea publicada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(view.context, "No existe el grupo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        return view
    }

    private fun crearTarea(tarea: NuevaTarea){
        val newTarea = NuevaTarea(tarea.id, tarea.autor, tarea.titulo, tarea.descripcion, tarea.grupo)
        val database = FirebaseDatabase.getInstance().getReference("NuevaTarea").child(tarea.grupo).child(tarea.id)
        database.setValue(newTarea)
    }

    private fun CargarTareas(){
        val database = FirebaseDatabase.getInstance().getReference("TareasCompletadas").child(usuario)
        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (tareaSnapshot in snapshot.children) {
                        val tareas = tareaSnapshot.getValue(NuevaTarea::class.java)
                            tareasArray.add(tareas!!)

                    }
                    var adaptadorsito = TareasListaAdapter(tareasArray)
                    tareasRV.adapter = adaptadorsito
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}