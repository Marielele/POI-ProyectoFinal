package com.carlele.groupnchat

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.carlele.groupnchat.models.Logros
import com.carlele.groupnchat.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.net.URI

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var db = FirebaseDatabase.getInstance()
    private var userRef = db.getReference("Usuarios")
    private lateinit var storageReference: StorageReference
    private lateinit var imgUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val name_user = findViewById<EditText>(R.id.txt_user)
        val correo = findViewById<EditText>(R.id.txt_correo)
        val spiner = findViewById<Spinner>(R.id.spn_carreras)
        val pass = findViewById<EditText>(R.id.txt_contra)
        val btn_registro = findViewById<Button>(R.id.btn_crear_cuenta)
        val btn_img = findViewById<ImageButton>(R.id.btn_agregar_img)
        val carreras = listOf("Carrera: ", "LM", "LF", "LA", "LCC", "LSTI", "LMAD")
        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, carreras)
        spiner.adapter = adaptador

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        btn_registro.setOnClickListener {
            if (spiner.selectedItemPosition.equals(0) || name_user.text.isEmpty() ||
                    correo.text.isEmpty() || pass.text.isEmpty()) {
                Toast.makeText(this, "Ingrese datos requeridos", Toast.LENGTH_SHORT).show()
            }
            else {
                //Registrar user
                val careerSelected = spiner.selectedItem.toString()
                val username = name_user.text.toString()
                val email = correo.text.toString()
                val password = pass.text.toString()
                val status = "offline"
                RegistrarUsuario(Usuario(username, email, careerSelected, password, status), uid.toString())
                AgregarFotoUsuario(username)
            }
        }

        btn_img.setOnClickListener {
            ObtenerPermisoFotos()
        }

    }

    private fun AgregarFotoUsuario(user: String) {
        //storageReference = FirebaseStorage.getInstance().getReference("Usuarios/"+auth.currentUser?.uid)
        storageReference = FirebaseStorage.getInstance().getReference("Usuarios/$user")
        storageReference.putFile(imgUri)
    }

    private fun ObtenerPermisoFotos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
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
                Toast.makeText(this, "Otorga el permiso de la galeria", Toast.LENGTH_SHORT).show()
            }
    }

    private val startForActivityGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imgUri = result.data?.data!!
            val img = findViewById<ImageView>(R.id.img_usuario)
            img.setImageURI(imgUri)
        }
    }

    private fun TomarFotoDeGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startForActivityGallery.launch(intent)
    }

    private fun RegistrarUsuario(usuario:Usuario, uid: String) {
        auth.createUserWithEmailAndPassword(usuario.correo, usuario.contrasena).addOnCompleteListener {
            if (it.isSuccessful) {
                var nombreuser = usuario.nombre
                var addname = UserProfileChangeRequest.Builder().setDisplayName(nombreuser).build()
                FirebaseAuth.getInstance().currentUser?.updateProfile(addname)
                val newUser = Usuario(usuario.nombre, usuario.correo, usuario.carrera, usuario.contrasena, usuario.estado)
                //userRef.child(uid).setValue(newUser)
                userRef.child(usuario.nombre).setValue(newUser)
                Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()
                logros(nombreuser)
            }
            else {
                Toast.makeText(this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logros(user: String){
        val bienvenidoLogro = Logros("¡Bienvenido!", "Crea una cuenta", true)
        val comunicateLogro = Logros("Comunicate", "Envia un mensaje", false)
        val agrupandoLogro = Logros("Agrupando", "Crea un chat grupal", false)
        val miraEstoLogro = Logros("¡Mira esto!", "Envia una imagen", false)
        val responsableLogro = Logros("Responsable", "Completa una tarea", false)
        val socializandoLogro = Logros("Socializando", "Entra a un chat grupal", false)
        val publicateLogro = Logros("Publicate", "Crea una publicacion", false)
        val partedeLogro = Logros("Parte de", "Crea un subgrupo", false)
        val aguafiestasLogro = Logros("Aguafiestas", "Crea una tarea", false)
        val arreglo = arrayListOf<Logros>(bienvenidoLogro, comunicateLogro, agrupandoLogro, miraEstoLogro,
        responsableLogro, socializandoLogro, publicateLogro, partedeLogro, aguafiestasLogro)
        db.getReference("Logros").child(user).setValue(arreglo)
    }



}