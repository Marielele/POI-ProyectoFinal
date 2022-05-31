package com.carlele.groupnchat.fragments

import android.Manifest
import android.app.Activity
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.adapters.MensajesRecientesListaAdapter
import com.carlele.groupnchat.models.ChatsGrupales
import com.carlele.groupnchat.models.Grupo
import com.carlele.groupnchat.models.Mensaje
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class MensajesFragment : Fragment() {

    private var db = FirebaseDatabase.getInstance()
    private var chatGroupsRef = db.getReference("Chats_grupales")
    private lateinit var database : DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imgUri: Uri
    private lateinit var chatRV: RecyclerView
    private lateinit var chatArray: ArrayList<Mensaje>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mensajes, container, false)
        val btn_nuevo_msg = view.findViewById<FloatingActionButton>(R.id.btn_new_msg)
        val btn_group_msg = view.findViewById<FloatingActionButton>(R.id.btn_new_msg_group)
        val currentUser = Firebase.auth.currentUser
        val admin = currentUser?.displayName.toString()

        chatRV = view.findViewById(R.id.rv_mensajes_recientes)
        chatArray = arrayListOf<Mensaje>()

        CargarMensajesRecientes(admin)

        btn_nuevo_msg.setOnClickListener {
            CambiarEntreFragments(NuevoMensajeFragment())
        }

        btn_group_msg.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            val v = layoutInflater.inflate(R.layout.layout_dialog_chat_grupal, null)
            builder.setView(v)
            val dialog = builder.create()
            dialog.show()

            val btn_crear = v.findViewById<Button>(R.id.btn_create)
            val txt_group_name = v.findViewById<EditText>(R.id.txt_chat_group_name)
            val btn_entrar = v.findViewById<Button>(R.id.btn_get_in)
            val btn_img = v.findViewById<ImageButton>(R.id.btn_image)

            btn_crear.setOnClickListener {
                if (txt_group_name.text.toString().isEmpty()) {
                    Toast.makeText(v.context, "Escriba nombre de chat grupal", Toast.LENGTH_SHORT).show()
                } else {
                    CrearChatGrupal(ChatsGrupales(txt_group_name.text.toString(), admin))
                    AgregarFotoGrupo(txt_group_name.text.toString())
                    abrirFragmentDatos(ChatGrupalFragment(), "chat", txt_group_name.text.toString())
                    dialog.hide()
                }
            }

            btn_entrar.setOnClickListener {
                if (txt_group_name.text.toString().isEmpty()) {
                    Toast.makeText(v.context, "Escriba nombre de chat grupal", Toast.LENGTH_SHORT).show()
                } else {
                    database = FirebaseDatabase.getInstance().getReference("Chats_grupales")
                    database.child(txt_group_name.text.toString()).get().addOnSuccessListener {
                        if (it.exists()) {
                            abrirFragmentDatos(ChatGrupalFragment(), "chat", txt_group_name.text.toString())
                            dialog.hide()
                        } else {
                            Toast.makeText(context, "No existe el chat buscado, cree uno", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btn_img.setOnClickListener {
                ObtenerPermisoDeFotos()
            }
        }

        return view
    }

    private fun CargarMensajesRecientes(usuario: String) {
        database = FirebaseDatabase.getInstance().getReference("Ultimo").child(usuario)
        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (msgSnapshot in snapshot.children) {
                        val msg = msgSnapshot.getValue(Mensaje::class.java)
                        chatArray.add(msg!!)
                    }
                    var adaptadorsito = MensajesRecientesListaAdapter(chatArray)
                    chatRV.adapter = adaptadorsito
                    adaptadorsito.setOnItemClickListener(object : MensajesRecientesListaAdapter.OnItemClickListener{
                        override fun OnItemClick(position: Int, receptor: String) {
                            val esGrupo = snapshot.child(receptor).child("group").value
                            if (esGrupo == true) {
                                abrirFragmentDatos(ChatGrupalFragment(),"chat", receptor)
                                //Toast.makeText(context, "grupo", Toast.LENGTH_SHORT).show()
                            } else {
                                abrirFragmentDatos(ChatFragment(), "receptor", receptor)
                                //Toast.makeText(context, "no grupo", Toast.LENGTH_SHORT).show()
                            }
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun AgregarFotoGrupo(grupo: String) {
        storageReference = FirebaseStorage.getInstance().getReference("Chats/$grupo")
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

    private fun TomarFotoDeGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startForActivityGallery.launch(intent)
    }


    private fun CambiarEntreFragments(fm: Fragment){
        val transactio: FragmentTransaction = parentFragmentManager.beginTransaction()
        transactio.replace(R.id.container, fm).commit()
    }

    private fun CrearChatGrupal(chat: ChatsGrupales){
        val newGroup = ChatsGrupales(chat.nombre, chat.admin)
        chatGroupsRef.child(chat.nombre).setValue(newGroup)
    }

    private fun abrirFragmentDatos(fragmento: Fragment,nombre: String, dato: String){
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        val myBundle = Bundle()
        myBundle.putString(nombre, dato)
        fragmento.arguments = myBundle
        transaction.replace(R.id.container, fragmento).commit()
    }


}