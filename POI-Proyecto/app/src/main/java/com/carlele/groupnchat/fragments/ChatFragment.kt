package com.carlele.groupnchat.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.adapters.MensajesListaAdapter
import com.carlele.groupnchat.models.Mensaje
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.sql.Timestamp
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class ChatFragment : Fragment() {

    private val listaMensajes = mutableListOf<Mensaje>()
    private val msglistAdapter = MensajesListaAdapter(listaMensajes)
    var usuario: String = ""
    var receptor: String = ""
    private val database = FirebaseDatabase.getInstance()
    private val chatRef = database.getReference("Chats")
    private val recentRef = database.getReference("Ultimo")
    private lateinit var db : DatabaseReference
    private lateinit var imgUri: Uri
    private lateinit var fileUri: Uri
    private lateinit var storageReference: StorageReference
    private lateinit var storageReference2: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        val titulo = view.findViewById<Toolbar>(R.id.tb_chat_title)
        val btn_send = view.findViewById<ImageButton>(R.id.btn_send)
        val texto = view.findViewById<EditText>(R.id.txt_msg)
        val recyclerview = view.findViewById<RecyclerView>(R.id.rv_lista_chat)
        val files = view.findViewById<ImageButton>(R.id.btn_send_file)
        val cb_encrypted = view.findViewById<CheckBox>(R.id.cb_encrypted)

        recyclerview.adapter = msglistAdapter

        val bundle = arguments
        val cont = bundle!!.getString("receptor")
        receptor = cont.toString()

        val user = Firebase.auth.currentUser
        val actualUser = user?.displayName
        usuario = actualUser.toString()

        titulo.title = receptor

        btn_send.setOnClickListener {
            val mensaje = texto.text.toString()
            val msg_encrypted = encrypt(mensaje,"uou11u3u0if4").toString()
            if (mensaje.isNotEmpty()) {
                texto.text.clear()
                if (cb_encrypted.isChecked) {
                    enviarMensaje(Mensaje("", msg_encrypted, usuario, ServerValue.TIMESTAMP, receptor, true), usuario, receptor)
                } else {
                    enviarMensaje(Mensaje("", mensaje, usuario, ServerValue.TIMESTAMP, receptor, false), usuario, receptor)
                }
            } else {
                Toast.makeText(context, "Escriba un mensaje a enviar", Toast.LENGTH_SHORT).show()
            }
        }

        files.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            val vista = layoutInflater.inflate(R.layout.layout_enviar_archivos, null)
            builder.setView(vista)
            val dialog = builder.create()
            dialog.show()
            val rb_img = vista.findViewById<RadioButton>(R.id.rb_choose_img)
            val rb_pdf = vista.findViewById<RadioButton>(R.id.rb_choose_file)
            val rg = vista.findViewById<RadioGroup>(R.id.rg_choose)
            val btn_img = vista.findViewById<ImageButton>(R.id.btn_add_img)
            val btn_file = vista.findViewById<ImageButton>(R.id.btn_add_file)
            val btn_enviar = vista.findViewById<Button>(R.id.btn_enviar_files)

            btn_img.setOnClickListener {
                TomarFotoDeGaleria()
            }
            btn_file.setOnClickListener {
                TomarArchivoDeGaleria()
            }
            btn_enviar.setOnClickListener {
                if (!rb_img.isChecked && !rb_pdf.isChecked) {
                    Toast.makeText(context, "Seleccione una opcion para enviar", Toast.LENGTH_SHORT).show()
                } else {
                    when (rb_img.isChecked) {
                        true -> {
                            val mensajito = Mensaje("", "", usuario, ServerValue.TIMESTAMP, receptor, false, true, false)
                            val idsito = "$usuario y $receptor".replace("\n", "").filter { !it.isWhitespace() }
                            val ref = chatRef.child(idsito)
                            val mensajeFireBase = ref.push()
                            mensajito.id = mensajeFireBase.key.toString()
                            mensajeFireBase.setValue(mensajito)
                            val aidi = mensajeFireBase.key.toString()
                            val idsito2 = "$receptor y $usuario".replace("\n", "").filter { !it.isWhitespace() }
                            val ref2 = chatRef.child(idsito2)
                            val mensajeFireBase2 = ref2.push()
                            //mensajito.id = mensajeFireBase2.key.toString()
                            mensajito.id = aidi
                            mensajeFireBase2.setValue(mensajito)
                            //val idryu = mensajeFireBase2.key.toString()

                            mensajeReciente(mensajito)

                            storageReference = FirebaseStorage.getInstance().getReference("Chats/$idsito/$aidi")
                            storageReference.putFile(imgUri)
                            storageReference2 = FirebaseStorage.getInstance().getReference("Chats/$idsito2/$aidi")
                            storageReference2.putFile(imgUri)
                        }
                        false -> {
                            val mensajito = Mensaje("", "", usuario, ServerValue.TIMESTAMP, receptor, false, false, true)
                            val idsito = "$usuario y $receptor".replace("\n", "").filter { !it.isWhitespace() }
                            val ref = chatRef.child(idsito)
                            val mensajeFireBase = ref.push()
                            mensajito.id = mensajeFireBase.key.toString()
                            mensajeFireBase.setValue(mensajito)
                            val aidi = mensajeFireBase.key.toString()
                            val idsito2 = "$receptor y $usuario".replace("\n", "").filter { !it.isWhitespace() }
                            val ref2 = chatRef.child(idsito2)
                            val mensajeFireBase2 = ref2.push()
                            mensajito.id = aidi
                            mensajeFireBase2.setValue(mensajito)

                            mensajeReciente(mensajito)

                            storageReference = FirebaseStorage.getInstance().getReference("Chats/$idsito/$aidi")
                            storageReference.putFile(fileUri)
                            storageReference2 = FirebaseStorage.getInstance().getReference("Chats/$idsito2/$aidi")
                            storageReference2.putFile(fileUri)
                        }
                    }
                }
            }
        }

        actualizarEstado(titulo)
        recibirMensajes(usuario, receptor, recyclerview)
        return view
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

    private fun actualizarEstado(tb: Toolbar) {
        db = FirebaseDatabase.getInstance().getReference("Usuarios")
        db.child(receptor).child("estado").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue().toString() == "offline") {
                    tb.setSubtitleTextColor(Color.GRAY)
                }
                tb.subtitle = snapshot.getValue().toString()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun recibirMensajes(usuario: String, receptor: String, rv: RecyclerView) {
        val idsito = "$usuario y $receptor".replace("\n", "").filter { !it.isWhitespace() }
        val refContacto = chatRef.child(idsito)
        refContacto.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                listaMensajes.clear()
                for (snap in snapshot.children){
                    val mensaje : Mensaje = snap.getValue(Mensaje::class.java) as Mensaje

                    if (mensaje.encrypt) {
                        val msg_decrypt = decrypt(mensaje.contenido, "uou11u3u0if4")
                        mensaje.contenido = msg_decrypt
                    }

                    if (mensaje.de == usuario) {
                        mensaje.esMio = true
                    }

                    listaMensajes.add(mensaje)
                }
                if (listaMensajes.size>0){
                    msglistAdapter.notifyDataSetChanged()
                    rv.smoothScrollToPosition(listaMensajes.size-1)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(view!!.context, "Error al leer mensajes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun enviarMensaje(mensaje: Mensaje, actualU: String, recep: String) {

        val mensajito = Mensaje(mensaje.id,mensaje.contenido, mensaje.de, mensaje.timeStamp,mensaje.para, mensaje.encrypt)
        val idsito = "$actualU y $recep".replace("\n", "").filter { !it.isWhitespace() }
        val ref = chatRef.child(idsito)
        val mensajeFireBase = ref.push()
        val key1 = mensajeFireBase.key.toString()
        mensaje.id = key1
        mensajeFireBase.setValue(mensajito)

        val idsito2 = "$recep y $actualU".replace("\n", "").filter { !it.isWhitespace() }
        val ref2 = chatRef.child(idsito2)
        val mensajeFireBase2 = ref2.push()
        val key2 = mensajeFireBase2.key.toString()
        mensaje.id = key2
        mensajeFireBase2.setValue(mensajito)

        mensajeReciente(mensajito)

    }

    private fun mensajeReciente(mensaje: Mensaje){
        val msg = Mensaje(mensaje.id, mensaje.contenido, mensaje.de, mensaje.timeStamp, mensaje.para, mensaje.encrypt, mensaje.hasImage, mensaje.hasFile)
        val idsito = "${mensaje.de} y ${mensaje.para}".replace("\n", "").filter { !it.isWhitespace() }
        val ref = recentRef.child(mensaje.de).child(idsito)
        ref.setValue(msg)

        val idsito2 = "${mensaje.para} y ${mensaje.de}".replace("\n", "").filter { !it.isWhitespace() }
        val ref2 = recentRef.child(mensaje.para).child(idsito2)
        ref2.setValue(msg)
    }

    //AES
    private fun encrypt(textoPlano: String, llave: String): String{
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")

        val llaveBytesFinal = ByteArray(16)
        val llaveByteOriginal = llave.toByteArray(charset("UTF-8"))

        System.arraycopy(
            llaveByteOriginal,
            0,
            llaveBytesFinal,
            0,
            Math.min(
                llaveByteOriginal.size,
                llaveBytesFinal.size
            )
        )


        //La llave
        val secretKeySpec: SecretKeySpec = SecretKeySpec(
            llaveBytesFinal,
            "AES"
        )
        //VECTOR DE INICIALIZACION NOSR SIRVE PARA EL CIFRADO
        val vectorInit = IvParameterSpec(llaveBytesFinal)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, vectorInit)

        //texto que queremos cifrar
        val textoCifrado = cipher.doFinal(textoPlano.toByteArray(charset("UTF-8")))

        val resultadoEnBase = String (Base64.encode(textoCifrado, Base64.NO_PADDING))
        return resultadoEnBase
    }

    private fun decrypt(textoCifrado: String, llave: String): String{

        //Descifra del base 64
        val textoCifradoBytes = Base64.decode(textoCifrado, Base64.NO_PADDING)

        //Descifrar del AES

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")

        val llaveBytesFinal = ByteArray(16)
        val llaveByteOriginal = llave.toByteArray(charset("UTF-8"))

        System.arraycopy(
            llaveByteOriginal,
            0,
            llaveBytesFinal,
            0,
            Math.min(
                llaveByteOriginal.size,
                llaveBytesFinal.size
            )
        )


        //La llave
        val secretKeySpec: SecretKeySpec = SecretKeySpec(
            llaveBytesFinal,
            "AES"
        )
        //Desciframos
        val vectorInit = IvParameterSpec(llaveBytesFinal)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, vectorInit)

        val textoPlano = String(cipher.doFinal(textoCifradoBytes))

        return textoPlano
    }

}