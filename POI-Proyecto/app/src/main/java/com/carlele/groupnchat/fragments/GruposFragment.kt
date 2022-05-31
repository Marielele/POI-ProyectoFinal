package com.carlele.groupnchat.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.adapters.GruposListaAdapter
import com.carlele.groupnchat.adapters.UsuariosListaAdapter
import com.carlele.groupnchat.models.ChatsGrupales
import com.carlele.groupnchat.models.Grupo
import com.carlele.groupnchat.models.Usuario
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class GruposFragment : Fragment() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var groupsRV: RecyclerView
    private lateinit var groupArray: ArrayList<Grupo>
    private lateinit var storageReference: StorageReference
    private lateinit var imgUri: Uri

    private lateinit var database : DatabaseReference

    private var db = FirebaseDatabase.getInstance()
    private var groupsRef = db.getReference("Grupos")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_grupos, container, false)
        val btn_add = view.findViewById<FloatingActionButton>(R.id.btn_add_group)

        groupsRV = view.findViewById(R.id.rv_lista_grupos)
        groupArray = arrayListOf<Grupo>()

        ObtenerGrupos()

        btn_add.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            val vista = layoutInflater.inflate(R.layout.layout_dialog_new_group, null)
            builder.setView(vista)
            val dialog = builder.create()
            dialog.show()

            val group_name = vista.findViewById<EditText>(R.id.txt_new_group)
            val btn_create = vista.findViewById<Button>(R.id.btn_new_group)
            val spn = vista.findViewById<Spinner>(R.id.spinner_carreras)
            val carreras = listOf("Carrera: ", "LM", "LF", "LA", "LCC", "LSTI", "LMAD")
            val adapter = ArrayAdapter(vista.context,  android.R.layout.simple_spinner_item, carreras)
            val btn_img = vista.findViewById<ImageButton>(R.id.btn_img_grupo)

            spn.adapter = adapter

            btn_create.setOnClickListener {
                if (group_name.text.toString().isEmpty()) {
                    Toast.makeText(vista.context, "Escriba nombre de grupo", Toast.LENGTH_SHORT).show()
                } else {
                    if (spn.selectedItem.toString().equals("Carrera: ")){
                        Toast.makeText(vista.context, "Seleccione carrera", Toast.LENGTH_SHORT).show()
                    } else {
                        CrearGrupo(Grupo("", group_name.text.toString(), spn.selectedItem.toString()))
                        AgregarFotoGrupo(group_name.text.toString())
                        dialog.hide()
                    }
                }
            }

            btn_img.setOnClickListener {
                ObtenerPermisoDeFotos()
            }

        }

        return view
    }

    private fun AgregarFotoGrupo(grupo: String) {
        storageReference = FirebaseStorage.getInstance().getReference("Grupos/$grupo")
        storageReference.putFile(imgUri)
    }

    private fun ObtenerPermisoDeFotos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    TomarFotoDeGaleria()
                }

                else -> requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            TomarFotoDeGaleria()
        }
    }

    private fun TomarFotoDeGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startForActivityGallery.launch(intent)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
            isGranted ->
        if (isGranted) {
            TomarFotoDeGaleria()
        } else {
            Toast.makeText(context, "Otorga el permiso de la galeria", Toast.LENGTH_SHORT).show()
        }
    }

    private val startForActivityGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imgUri = result.data?.data!!
        }
    }

    private fun CrearGrupo(grupo: Grupo) {
        val newGroup = Grupo(grupo.id, grupo.nombre, grupo.carrera)
        groupsRef.child(grupo.nombre).setValue(newGroup)
    }

    private fun ObtenerGrupos() {
        val currentUser = Firebase.auth.currentUser
        database = FirebaseDatabase.getInstance().getReference("Usuarios")
        database.child(currentUser?.displayName.toString()).child("carrera").get().addOnSuccessListener {
            if (it.exists()) {
                val carrera = it.value
                dbRef = FirebaseDatabase.getInstance().getReference("Grupos")
                dbRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (userSnapshot in snapshot.children) {
                                val group = userSnapshot.getValue(Grupo::class.java)
                                if (group!!.carrera.equals(carrera)) {
                                    groupArray.add(group!!)
                                }
                            }
                            var adatadorsito = GruposListaAdapter(groupArray)
                            groupsRV.layoutManager = GridLayoutManager(context, 3)
                            groupsRV.adapter = adatadorsito
                            adatadorsito.setOnItemClickListener(object : GruposListaAdapter.OnItemClickListener{
                                override fun OnItemClick(position: Int, nombre: String) {
                                    abrirFragmentDatos(GrupoFragment(), nombre)
                                }
                            })
                        }
                        else {
                            Toast.makeText(context, "no existe unu", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            }
        }

    }

    private fun abrirFragmentDatos(fragmento: Fragment, dato: String){
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        val myBundle = Bundle()
        myBundle.putString("groupname", dato)
        fragmento.arguments = myBundle
        transaction.replace(R.id.container, fragmento).commit()
    }


}