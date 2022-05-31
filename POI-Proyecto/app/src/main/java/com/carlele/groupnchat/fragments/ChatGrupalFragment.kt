package com.carlele.groupnchat.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.carlele.groupnchat.R
import com.carlele.groupnchat.adapters.MensajesGrupalesListaAdapter
import com.carlele.groupnchat.adapters.MensajesListaAdapter
import com.carlele.groupnchat.models.Mensaje
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class ChatGrupalFragment : Fragment() {
    private val lista = mutableListOf<Mensaje>()
    private val adaptador = MensajesGrupalesListaAdapter(lista)
    var usuario: String = ""
    var grupo: String = ""
    private val database = FirebaseDatabase.getInstance()
    private val chatRef = database.getReference("Chats")
    private lateinit var imgUri: Uri
    private lateinit var fileUri: Uri
    private lateinit var storageReference: StorageReference
    private val recentRef = database.getReference("Ultimo")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat_grupal, container, false)

        val titulo = view.findViewById<Toolbar>(R.id.tb_title_chat_group)
        val btn_send = view.findViewById<ImageButton>(R.id.btn_enviar)
        val texto = view.findViewById<EditText>(R.id.txt_texto)
        val recyclerview = view.findViewById<RecyclerView>(R.id.rv_chat_group)
        val cb_encrypt = view.findViewById<CheckBox>(R.id.cb_toencrypt)
        val btn_files = view.findViewById<ImageButton>(R.id.bnt_attach_file)

        recyclerview.adapter = adaptador

        val bundle = arguments
        val cont = bundle!!.getString("chat")
        grupo = cont.toString()

        titulo.title = grupo

        val user = Firebase.auth.currentUser
        val actualUser = user?.displayName
        usuario = actualUser.toString()

        btn_send.setOnClickListener {
            val mensaje = texto.text.toString()
            val msg_encrypted = encrypt(mensaje,"uou11u3u0if4").toString()
            if (mensaje.isNotEmpty()) {
                texto.text.clear()
                if (cb_encrypt.isChecked){
                    enviarMensaje(Mensaje("", msg_encrypted, usuario, ServerValue.TIMESTAMP, grupo, true, false, false, true), grupo)
                } else {
                    enviarMensaje(Mensaje("", mensaje, usuario, ServerValue.TIMESTAMP, grupo, false, false, false, true), grupo)
                }
            } else {
                Toast.makeText(context, "Escriba un mensaje a enviar", Toast.LENGTH_SHORT).show()
            }
        }

        btn_files.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            val vista = layoutInflater.inflate(R.layout.layout_enviar_archivos, null)
            builder.setView(vista)
            val dialog = builder.create()
            dialog.show()

            val rb_img = vista.findViewById<RadioButton>(R.id.rb_choose_img)
            val rb_pdf = vista.findViewById<RadioButton>(R.id.rb_choose_file)
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
                            val mensajito = Mensaje("", "", usuario, ServerValue.TIMESTAMP, grupo, false, true, false, true)
                            val idsito = "$grupo".replace("\n", "").filter { !it.isWhitespace() }
                            val ref = chatRef.child(grupo)
                            val mensajeFireBase = ref.push()
                            mensajito.id = mensajeFireBase.key.toString()
                            mensajeFireBase.setValue(mensajito)
                            val aidi = mensajeFireBase.key.toString()

                            mensajeReciente(mensajito)

                            storageReference = FirebaseStorage.getInstance().getReference("Chats/$grupo/$aidi")
                            storageReference.putFile(imgUri)
                        }
                        false -> {
                            val mensajito = Mensaje("", "", usuario, ServerValue.TIMESTAMP, grupo, false, false, true, true)
                            val idsito = "$grupo".replace("\n", "").filter { !it.isWhitespace() }
                            val ref = chatRef.child(grupo)
                            val mensajeFireBase = ref.push()
                            mensajito.id = mensajeFireBase.key.toString()
                            mensajeFireBase.setValue(mensajito)
                            val aidi = mensajeFireBase.key.toString()

                            mensajeReciente(mensajito)

                            storageReference = FirebaseStorage.getInstance().getReference("Chats/$grupo/$aidi")
                            storageReference.putFile(fileUri)
                        }
                    }
                }
            }
        }

        recibirMensajes(usuario, grupo, recyclerview)

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

    private fun recibirMensajes(usuario: String, grupo: String, recyclerview: RecyclerView) {
        val idsito = "$grupo".replace("\n", "").filter { !it.isWhitespace() }
        val refContacto = chatRef.child(grupo)
        refContacto.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                lista.clear()
                for (snap in snapshot.children){
                    val mensaje : Mensaje = snap.getValue(Mensaje::class.java) as Mensaje

                    if (mensaje.encrypt) {
                        val msg_decrypt = decrypt(mensaje.contenido, "uou11u3u0if4")
                        mensaje.contenido = msg_decrypt
                    }

                    if (mensaje.de == usuario) {
                        mensaje.esMio = true
                    }

                    lista.add(mensaje)
                }
                if (lista.size>0){
                    adaptador.notifyDataSetChanged()
                    recyclerview.smoothScrollToPosition(lista.size-1)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(view!!.context, "Error al leer mensajes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun enviarMensaje(mensaje: Mensaje, grupo: String) {
        val mensajito = Mensaje(mensaje.id,mensaje.contenido, mensaje.de, mensaje.timeStamp,mensaje.para, mensaje.encrypt, mensaje.hasImage, mensaje.hasFile, mensaje.isGroup)
        val idsito = "$grupo".replace("\n", "").filter { !it.isWhitespace() }
        val ref = chatRef.child(grupo)
        val mensajeFireBase = ref.push()
        mensaje.id = mensajeFireBase.key ?: ""
        mensajeFireBase.setValue(mensajito)

        mensajeReciente(mensajito)
    }

    private fun mensajeReciente(mensaje: Mensaje){
        val msg = Mensaje(mensaje.id, mensaje.contenido, mensaje.de, mensaje.timeStamp, mensaje.para, mensaje.encrypt, mensaje.hasImage, mensaje.hasFile, mensaje.isGroup)
        val idsito = mensaje.para.replace("\n", "").filter { !it.isWhitespace() }
        val ref = recentRef.child(mensaje.de).child(mensaje.para)
        ref.setValue(msg)
    }
}