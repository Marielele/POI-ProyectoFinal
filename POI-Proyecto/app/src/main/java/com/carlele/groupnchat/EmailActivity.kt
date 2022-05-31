package com.carlele.groupnchat

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class EmailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        val emails = findViewById<EditText>(R.id.txt_email)
        val asunto = findViewById<EditText>(R.id.txt_asunto)
        val mensaje = findViewById<EditText>(R.id.txt_email_contenido)
        val btn_enviar = findViewById<Button>(R.id.btn_enviar_email)

        btn_enviar.setOnClickListener {
            if (emails.text.isEmpty() || asunto.text.isEmpty() || mensaje.text.isEmpty()) {
                Toast.makeText(this, "Escribe lo pedido", Toast.LENGTH_SHORT).show()
            } else {
                val direcciones = emails.text.toString().split(",".toRegex()).toTypedArray()
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, direcciones)
                    putExtra(Intent.EXTRA_SUBJECT, asunto.text.toString())
                    putExtra(Intent.EXTRA_TEXT, mensaje.text.toString())
                }

                if (intent.resolveActivity(packageManager)!= null){
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "No tiene app para enviar emails, porfavor descargue una primero", Toast.LENGTH_SHORT).show()
                }

            }
        }

    }
}