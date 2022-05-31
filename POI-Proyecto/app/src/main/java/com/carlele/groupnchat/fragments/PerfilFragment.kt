package com.carlele.groupnchat.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.carlele.groupnchat.MainActivity
import com.carlele.groupnchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class PerfilFragment : Fragment() {

    private lateinit var database : DatabaseReference
    private lateinit var authe : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)
        val img = view.findViewById<CircleImageView>(R.id.img_user_pic)
        val nombre_usuario = view.findViewById<TextView>(R.id.tv_user_profile)
        val carrera = view.findViewById<TextView>(R.id.tv_carrera)
        val email = view.findViewById<TextView>(R.id.tv_email)
        val btn_logout = view.findViewById<ImageButton>(R.id.btn_logout)

        authe = FirebaseAuth.getInstance()
        val current_user = Firebase.auth.currentUser
        if (current_user != null) {
            nombre_usuario.text = current_user.displayName
            email.text = current_user.email

            database = FirebaseDatabase.getInstance().getReference("Usuarios")
            database.child(current_user.displayName.toString()).get().addOnSuccessListener {
                if (it.exists()) {
                    val career = it.child("carrera").value
                    carrera.text = career.toString()
                }
            }
        }

        val storageRef = FirebaseStorage.getInstance().reference.child("Usuarios/${nombre_usuario.text}")

        val localFileJPG = File.createTempFile("tempImg", "jpg")
        val localFilePNG = File.createTempFile("tempImg", "png")
        val localFileJPEG = File.createTempFile("tempImg", "jpeg")

        storageRef.getFile(localFileJPEG).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFileJPEG.absolutePath)
            img.setImageBitmap(bitmap)
        }.addOnFailureListener {
            storageRef.getFile(localFileJPG).addOnSuccessListener {
                val bmp = BitmapFactory.decodeFile(localFileJPG.absolutePath)
                img.setImageBitmap(bmp)
            }.addOnFailureListener {
                storageRef.getFile(localFilePNG).addOnSuccessListener {
                    val bit = BitmapFactory.decodeFile(localFilePNG.absolutePath)
                    img.setImageBitmap(bit)
                }.addOnFailureListener {
                    Toast.makeText(view.context, "Solo JPG, PNG y JPEG", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btn_logout.setOnClickListener {
            database = FirebaseDatabase.getInstance().getReference("Usuarios")
            database.child(current_user?.displayName.toString()).child("estado").setValue("offline")
            authe.signOut()
            val ini = Intent(activity, MainActivity::class.java)
            activity?.startActivity(ini)
            activity?.finish()
        }

        return view
    }
}