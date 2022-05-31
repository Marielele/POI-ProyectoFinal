package com.carlele.groupnchat

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private val db = FirebaseDatabase.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn_regis = findViewById<TextView>(R.id.tv_regis)
        val btn_entrar = findViewById<Button>(R.id.btn_entrar)
        val et_correo = findViewById<EditText>(R.id.txt_email)
        val et_contra = findViewById<EditText>(R.id.txt_pass)

        auth = FirebaseAuth.getInstance()

        btn_regis.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        btn_entrar.setOnClickListener {
            val correo = et_correo.text.toString()
            val contra = et_contra.text.toString()

            if (correo.isEmpty() || contra.isEmpty()) {
                Toast.makeText(this, "Escriba los datos requeridos", Toast.LENGTH_SHORT).show()
            }
            else {
                auth.signInWithEmailAndPassword(correo, contra).addOnCompleteListener {
                    if (it.isSuccessful) {
                        startActivity(Intent(this, PrincipalActivity::class.java))
                        val current = auth.currentUser?.displayName.toString()
                        db.getReference("Usuarios").child(current).child("estado").setValue("online")
                    }
                    else {
                        Toast.makeText(this, "Error al iniciar sesion", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle("Seleccione una opcion")
            setMessage("Salir de la aplicacion")

            setPositiveButton("Si") { _, _ ->
                // if user press yes, then finish the current activity
                super.onBackPressed()
            }

            setNegativeButton("No"){_, _ ->
                // if user press no, then return the activity
                Toast.makeText(this@MainActivity, "UwU",
                    Toast.LENGTH_LONG).show()
            }

            setCancelable(true)
        }.create().show()
    }
}