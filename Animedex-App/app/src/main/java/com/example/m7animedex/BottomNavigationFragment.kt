package com.example.m7animedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento que contiene el BottomNavigationView
        val view = inflater.inflate(R.layout.fragment_bottom_navigation, container, false)

        // Referencia al BottomNavigationView
        val bottomNavigationView: BottomNavigationView = view.findViewById(R.id.bottom_navigation)

        bottomNavigationView.menu.setGroupCheckable(0, true, false)

        // Establecer el ítem seleccionado según el fragmento actual
        when (parentFragmentManager.findFragmentById(R.id.fragment_container)) {
            is HomeFragment -> bottomNavigationView.selectedItemId = R.id.nav_home
            is ListsFragment -> bottomNavigationView.selectedItemId = R.id.nav_lists
            is SearchFragment -> bottomNavigationView.selectedItemId = R.id.nav_search
            else -> bottomNavigationView.selectedItemId = View.NO_ID
        }

        // Configurar el listener para manejar la navegación por fragments
        bottomNavigationView.setOnItemSelectedListener { item ->
            val selectedFragment = when (item.itemId) {
                R.id.nav_search -> SearchFragment()
                R.id.nav_home -> HomeFragment()
                R.id.nav_lists -> ListsFragment()
                // Nuevo fragmento de estadísticas
                else -> return@setOnItemSelectedListener false
            }

            // Reemplazar el fragmento actual con el seleccionado
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()

            true
        }

        return view
    }
}