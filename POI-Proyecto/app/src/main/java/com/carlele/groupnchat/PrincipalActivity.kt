package com.carlele.groupnchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.carlele.groupnchat.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class PrincipalActivity : AppCompatActivity() {

    private val inicio_frag = InicioFragment()
    private val msg_frag = MensajesFragment()
    private val grupos_frag = GruposFragment()
    private val tareas_frag = TareasFragment()
    private val perfil_frag = PerfilFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        val navbar = findViewById<BottomNavigationView>(R.id.botom_nav)

        CambioFragments(inicio_frag)

        navbar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_inicio -> {
                    CambioFragments(inicio_frag)
                }
                R.id.item_msg -> {
                    CambioFragments(msg_frag)
                }
                R.id.item_grupos -> {
                    CambioFragments(grupos_frag)
                }
                R.id.item_tareas -> {
                    CambioFragments(tareas_frag)
                }
                R.id.item_perfil -> {
                    CambioFragments(perfil_frag)
                }
            }
            true
        }

    }

    private fun CambioFragments(fm: Fragment) {
        if (fm != null) {
            val trans = supportFragmentManager.beginTransaction()
            trans.replace(R.id.container, fm)
            trans.commit()
        }
    }
}