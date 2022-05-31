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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.adapters.PublicacionesAdapter
import com.carlele.groupnchat.models.Publicacion
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SubgrupoFragment : Fragment() {

    private var db = FirebaseDatabase.getInstance()
    private var publishRef = db.getReference("Publicaciones")
    var subgrupoActual : String = ""
    private lateinit var storageReference: StorageReference
    private lateinit var imgUri: Uri
    private lateinit var fileUri: Uri
    private lateinit var database : DatabaseReference
    private lateinit var publisRV: RecyclerView
    private lateinit var publisArray: ArrayList<Publicacion>
    private lateinit var idFile : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_subgrupo, container, false)

        val rv_publis = v.findViewById<RecyclerView>(R.id.rv_publis_sub)
        val tv_publi = v.findViewById<TextView>(R.id.tv_new_publi_sub)
        val titulo = v.findViewById<Toolbar>(R.id.tb_subgroup_name)

        val bundle = arguments
        val cont = bundle!!.getString("subgrupo")
        subgrupoActual = cont.toString()
        titulo.title = subgrupoActual

        publisRV = rv_publis
        publisArray = arrayListOf<Publicacion>()
        ObtenerPublicaciones()

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
                        CreaPublicacion(Publicacion(ide, texto.text.toString(), actualUser.toString(), true, true, subgrupoActual))
                        AgregarFotoGrupo(ide)
                        AgregarArchivo(ide)
                        //Subir Archivo
                    } else if (cb_img.isChecked && !cb_file.isChecked){
                        CreaPublicacion(Publicacion(ide, texto.text.toString(), actualUser.toString(), true, false, subgrupoActual))
                        AgregarFotoGrupo(ide)
                    } else if (!cb_img.isChecked && cb_file.isChecked) {
                        CreaPublicacion(Publicacion(ide, texto.text.toString(), actualUser.toString(), false, true, subgrupoActual))
                        AgregarArchivo(ide)
                        //Subir archivo
                    } else {
                        CreaPublicacion(Publicacion(ide, texto.text.toString(), actualUser.toString(), false, false, subgrupoActual))
                    }
                    Toast.makeText(context, "Se ha publicado", Toast.LENGTH_SHORT).show()
                }
            }

            add_arch.setOnClickListener {
                TomarArchivoDeGaleria()
            }

            add_img.setOnClickListener {
                TomarFotoDeGaleria()
            }

        }

        return v
    }

    private fun TomarFotoDeGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startForActivityGallery.launch(intent)
    }

    private val startForActivityGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imgUri = result.data?.data!!
        }
    }

    private fun TomarArchivoDeGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startForActivityGalleryo.launch(intent)
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
        publishRef.child(subgrupoActual).child(publi.contenido.replace("\n", "").filter { !it.isWhitespace() }).setValue(newPubli)
    }

    private fun AgregarFotoGrupo(texto: String) {
        storageReference = FirebaseStorage.getInstance().getReference("Publicaciones/$subgrupoActual/$texto/image")
        storageReference.putFile(imgUri)
    }

    private fun AgregarArchivo(texto : String) {
        storageReference = FirebaseStorage.getInstance().getReference("Publicaciones/$subgrupoActual/$texto/file")
        storageReference.putFile(fileUri)
    }

    private fun ObtenerPublicaciones(){
        database = FirebaseDatabase.getInstance().getReference("Publicaciones")
        database.child(subgrupoActual).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val publis = userSnapshot.getValue(Publicacion::class.java)
                        if (publis?.grupo.equals(subgrupoActual)) {
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
                TODO("Not yet implemented")
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
                    ComienzaDescarga(subgrupoActual,idFile)
                }

                else -> requestPermissionLauncher2.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            ComienzaDescarga(subgrupoActual,idFile)
        }
    }

    private fun ComienzaDescarga(grupo: String, carpeta: String) {
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
            ComienzaDescarga(subgrupoActual,idFile)
        } else {
            Toast.makeText(context, "Otorga el permiso de para descargar archivos", Toast.LENGTH_SHORT).show()
        }
    }

}