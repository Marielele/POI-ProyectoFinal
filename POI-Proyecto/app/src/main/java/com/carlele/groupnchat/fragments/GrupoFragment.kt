package com.carlele.groupnchat.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.EmailActivity
import com.carlele.groupnchat.R
import com.carlele.groupnchat.adapters.PublicacionesAdapter
import com.carlele.groupnchat.adapters.SubgruposListaAdapter
import com.carlele.groupnchat.adapters.TareasListaAdapter
import com.carlele.groupnchat.models.NuevaTarea
import com.carlele.groupnchat.models.Publicacion
import com.carlele.groupnchat.models.Subgrupo
import com.carlele.groupnchat.models.Usuario
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class GrupoFragment : Fragment() {

    private var db = FirebaseDatabase.getInstance()
    private var publishRef = db.getReference("Publicaciones")
    private lateinit var publisRV: RecyclerView
    private lateinit var publisArray: ArrayList<Publicacion>
    private lateinit var subsRV: RecyclerView
    private lateinit var subsArray: ArrayList<Subgrupo>
    private lateinit var tareasRV: RecyclerView
    private lateinit var tareasArray: ArrayList<NuevaTarea>
    private lateinit var storageReference: StorageReference
    private lateinit var database : DatabaseReference
    private lateinit var data : DatabaseReference

    var grupoActual : String = ""
    private lateinit var imgUri: Uri
    private lateinit var fileUri: Uri
    private lateinit var idFile : String
    var usuario: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_grupo, container, false)
        val btn_up = v.findViewById<ImageView>(R.id.img_up)
        val btn_down = v.findViewById<ImageView>(R.id.img_down)
        val rv_publis = v.findViewById<RecyclerView>(R.id.rv_publis)
        val btn_up2 = v.findViewById<ImageView>(R.id.img_up2)
        val btn_down2 = v.findViewById<ImageView>(R.id.img_down2)
        val rv_sub = v.findViewById<RecyclerView>(R.id.rv_subgrupos)
        val tv_publi = v.findViewById<TextView>(R.id.tv_new_publi)
        val tv_sub = v.findViewById<TextView>(R.id.tv_new_sub)
        val btn_up3 = v.findViewById<ImageView>(R.id.img_up3)
        val btn_down3 = v.findViewById<ImageView>(R.id.img_down3)
        val rv_tareas = v.findViewById<RecyclerView>(R.id.rv_tareas)
        val titulo = v.findViewById<Toolbar>(R.id.tb_group_name)
        val btn_email = v.findViewById<FloatingActionButton>(R.id.btn_email)

        val bundle = arguments
        val cont = bundle!!.getString("groupname")
        grupoActual = cont.toString()
        titulo.title = grupoActual

        val user = Firebase.auth.currentUser
        val actualUser = user?.displayName
        usuario = actualUser.toString()

        btn_up.setOnClickListener {
            btn_up.isVisible = false
            btn_down.isVisible = true
            rv_publis.isVisible = false
            tv_publi.isVisible = false
        }
        btn_down.setOnClickListener {
            btn_down.isVisible = false
            btn_up.isVisible = true
            rv_publis.isVisible = true
            tv_publi.isVisible = true
        }

        btn_up2.setOnClickListener {
            btn_up2.isVisible = false
            btn_down2.isVisible = true
            rv_sub.isVisible = false
            tv_sub.isVisible = false
        }
        btn_down2.setOnClickListener {
            btn_down2.isVisible = false
            btn_up2.isVisible = true
            rv_sub.isVisible = true
            tv_sub.isVisible = true
        }

        btn_up3.setOnClickListener {
            btn_up3.isVisible = false
            btn_down3.isVisible = true
            rv_tareas.isVisible = false
        }
        btn_down3.setOnClickListener {
            btn_down3.isVisible = false
            btn_up3.isVisible = true
            rv_tareas.isVisible = true
        }

        publisRV = rv_publis
        publisArray = arrayListOf<Publicacion>()
        ObtenerPublicaciones()

        subsRV = rv_sub
        subsArray = arrayListOf<Subgrupo>()
        ObtenerSubgrupos()

        tareasRV = rv_tareas
        tareasArray = arrayListOf<NuevaTarea>()
        ObtenerTareas()

        tv_publi.setOnClickListener {
            val builder = AlertDialog.Builder(v.context)
            val vista = layoutInflater.inflate(R.layout.layout_nueva_publicacion, null)
            builder.setView(vista)
            val dialog = builder.create()
            dialog.show()

            val texto = vista.findViewById<EditText>(R.id.txt_text)
            val add_img = vista.findViewById<ImageButton>(R.id.img_add_img)
            val add_arch = vista.findViewById<ImageButton>(R.id.img_add_archive)
            val btn_publicar = vista.findViewById<Button>(R.id.btn_publish)
            val cb_img = vista.findViewById<CheckBox>(R.id.cb_img)
            val cb_file = vista.findViewById<CheckBox>(R.id.cb_file)

            btn_publicar.setOnClickListener {
                if (texto.text.isEmpty()) {
                    Toast.makeText(context, "Escriba texto para publicar", Toast.LENGTH_SHORT).show()
                } else {
                    val user = Firebase.auth.currentUser
                    val actualUser = user?.displayName
                    val ide = texto.text.toString().replace("\n", "").filter { !it.isWhitespace() }
                    if (cb_img.isChecked && cb_file.isChecked) {
                        CreaPublicacion(Publicacion(ide, texto.text.toString(), actualUser.toString(), true, true, grupoActual))
                        AgregarFotoGrupo(ide)
                        AgregarArchivo(ide)
                        //Subir Archivo
                    } else if (cb_img.isChecked && !cb_file.isChecked){
                        CreaPublicacion(Publicacion(ide, texto.text.toString(), actualUser.toString(), true, false, grupoActual))
                        AgregarFotoGrupo(ide)
                    } else if (!cb_img.isChecked && cb_file.isChecked) {
                        CreaPublicacion(Publicacion(ide, texto.text.toString(), actualUser.toString(), false, true, grupoActual))
                        AgregarArchivo(ide)
                        //Subir archivo
                    } else {
                        CreaPublicacion(Publicacion(ide, texto.text.toString(), actualUser.toString(), false, false, grupoActual))
                    }

                    Toast.makeText(context, "Se ha publicado", Toast.LENGTH_SHORT).show()


                }
            }

            add_arch.setOnClickListener {
                TomarArchivoDeGaleria()
            }

            add_img.setOnClickListener {
                ObtenerPermisoDeFotos()
            }

        }

        tv_sub.setOnClickListener {
            val builder = AlertDialog.Builder(v.context)
            val vista = layoutInflater.inflate(R.layout.layout_nuevo_subgrupo, null)
            builder.setView(vista)
            val dialog = builder.create()
            dialog.show()

            val nombre_sub = vista.findViewById<EditText>(R.id.txt_subgrupo_name)
            val integrantes = vista.findViewById<EditText>(R.id.txt_nombre_integrante)
            val btn_add = vista.findViewById<ImageButton>(R.id.btn_add_member)
            val btn_crear = vista.findViewById<Button>(R.id.btn_crear_subgrupo)

            val user = Firebase.auth.currentUser
            val actualUser = user?.displayName
            val members = mutableListOf<String>(actualUser.toString())

            btn_add.setOnClickListener {
                if (integrantes.text.toString().isEmpty()) {
                    Toast.makeText(context, "Escriba un integrante para añadirlo", Toast.LENGTH_SHORT).show()
                } else {
                    database = FirebaseDatabase.getInstance().getReference("Usuarios")
                    database.child(actualUser.toString()).get().addOnSuccessListener { it1 ->
                        val carreraUserActual = it1.child("carrera").getValue()

                        database.child(integrantes.text.toString()).get().addOnSuccessListener {
                            val carreraUser = it.child("carrera").getValue()
                            if (it.exists() && carreraUser.toString() == carreraUserActual.toString()) {
                                members.add(integrantes.text.toString())
                                integrantes.text.clear()
                                Toast.makeText(context, members.toString(), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error, usuario no existente o no esta en la misma carrera", Toast.LENGTH_SHORT).show()
                            }
                            //Toast.makeText(context, "${carreraUser.toString()} y ${carreraUserActual.toString()}", Toast.LENGTH_SHORT).show()
                        }

                    }

                }
            }

            btn_crear.setOnClickListener {
                if (nombre_sub.text.isEmpty()) {
                    Toast.makeText(context, "Escriba nombre de subgrupo", Toast.LENGTH_SHORT).show()
                } else {
                    data = FirebaseDatabase.getInstance().getReference("SubGrupos")
                    database = FirebaseDatabase.getInstance().getReference("Usuarios")
                    database.child(actualUser.toString()).child("carrera").get().addOnSuccessListener {
                        val new = Subgrupo(nombre_sub.text.toString(), nombre_sub.text.toString(), members, it.getValue().toString(), grupoActual)
                        data.child(nombre_sub.text.toString()).setValue(new)
                        Toast.makeText(context, "Se ha creado el subgrupo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btn_email.setOnClickListener {
            var intent = Intent(activity, EmailActivity::class.java)
            activity?.startActivity(intent)
        }

        return v
    }

    private fun ObtenerTareas() {
        database = FirebaseDatabase.getInstance().getReference("NuevaTarea").child(grupoActual)
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

    private fun ObtenerSubgrupos() {
        database = FirebaseDatabase.getInstance().getReference("SubGrupos")
        val user = Firebase.auth.currentUser
        val actualUser = user?.displayName.toString()

        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val subs = userSnapshot.getValue(Subgrupo::class.java)
                        val intengrantes = subs?.integrantes?.contains(actualUser)
                        if (subs?.grupo.equals(grupoActual) && intengrantes == true) {
                            subsArray.add(subs!!)
                        }
                    }
                    var adatadorsito = SubgruposListaAdapter(subsArray)
                    subsRV.adapter = adatadorsito
                    adatadorsito.setOnItemClickListener(object: SubgruposListaAdapter.OnItemClickListener{
                        override fun OnItemClick(position: Int, nombre: String) {
                            Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
                            abrirFragmentDatos(SubgrupoFragment(), nombre)
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun abrirFragmentDatos(fragmento: Fragment, dato: String){
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        val myBundle = Bundle()
        myBundle.putString("subgrupo", dato)
        fragmento.arguments = myBundle
        transaction.replace(R.id.container, fragmento).commit()
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

    private fun TomarArchivoDeGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startForActivityGalleryo.launch(intent)
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

    private val startForActivityGalleryo = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fileUri = result.data?.data!!
        }
    }


    private fun CreaPublicacion(publi: Publicacion) {
        val newPubli =  Publicacion(publi.id, publi.contenido, publi.autor, publi.hasImg, publi.hasFile, publi.grupo)
        publishRef.child(grupoActual).child(publi.contenido.replace("\n", "").filter { !it.isWhitespace() }).setValue(newPubli)
    }

    private fun AgregarFotoGrupo(texto: String) {
        storageReference = FirebaseStorage.getInstance().getReference("Publicaciones/$grupoActual/$texto/image")
        storageReference.putFile(imgUri)
    }

    private fun AgregarArchivo(texto : String) {
        storageReference = FirebaseStorage.getInstance().getReference("Publicaciones/$grupoActual/$texto/file")
        storageReference.putFile(fileUri)
    }

    private fun ObtenerPublicaciones() {
        database = FirebaseDatabase.getInstance().getReference("Publicaciones")
        database.child(grupoActual).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val publis = userSnapshot.getValue(Publicacion::class.java)
                        if (publis?.grupo.equals(grupoActual)) {
                            publisArray.add(publis!!)
                        }
                    }
                    var adatadorsito = PublicacionesAdapter(publisArray)
                    publisRV.adapter = adatadorsito
                    adatadorsito.setOnItemClickListener(object: PublicacionesAdapter.OnItemClickListener{
                        override fun OnItemClick(position: Int, contenido: String) {

                            val verdad = snapshot.child(contenido.replace("\n", "").filter { !it.isWhitespace() }).child("hasFile").getValue()
                            idFile = contenido.replace("\n", "").filter { !it.isWhitespace() }
                            if (verdad == true) {
                                val builder = AlertDialog.Builder(context)
                                val vista = layoutInflater.inflate(R.layout.layout_dialog_file, null)
                                builder.setView(vista)
                                val dialog = builder.create()
                                dialog.show()
                                val btn_descarga = vista.findViewById<Button>(R.id.btn_archivo)

                                btn_descarga.setOnClickListener {
                                    Toast.makeText(context, "Descargar archivo", Toast.LENGTH_SHORT).show()
                                    val contenidoName = contenido.replace("\n", "").filter { !it.isWhitespace() }
                                    PermisoDescarga()
                                }

                            } else {
                                val builder = AlertDialog.Builder(context)
                                val vista = layoutInflater.inflate(R.layout.layout_dialog_file, null)
                                builder.setView(vista)
                                val dialog = builder.create()
                                dialog.show()
                                val btn_descarga = vista.findViewById<Button>(R.id.btn_archivo)
                                val texto = vista.findViewById<TextView>(R.id.tv_no_hay_sistema)

                                btn_descarga.isVisible = false
                                texto.isVisible = true
                            }

                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    fun PermisoDescarga() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    ComienzaDescarga(grupoActual,idFile)
                }

                else -> requestPermissionLauncher2.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            ComienzaDescarga(grupoActual,idFile)
        }
    }

    private fun ComienzaDescarga(grupo: String,carpeta: String) {
        storageReference = FirebaseStorage.getInstance().getReference("Publicaciones/$grupo/$carpeta/file")
        storageReference.downloadUrl.addOnSuccessListener {
            val request = DownloadManager.Request(Uri.parse(it.toString()))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle("Descarga")
            request.setDescription("El archivo se esta descargando...")
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${System.currentTimeMillis()}")
            val manager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
        }.addOnFailureListener {
        }
    }

    private val requestPermissionLauncher2 = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
            isGranted ->
        if (isGranted) {
            ComienzaDescarga(grupoActual,idFile)
        } else {
            Toast.makeText(context, "Otorga el permiso de para descargar archivos", Toast.LENGTH_SHORT).show()
        }
    }


}