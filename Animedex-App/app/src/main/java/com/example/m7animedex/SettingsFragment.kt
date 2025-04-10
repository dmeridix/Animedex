package com.example.m7animedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        val deleteAccountButton = view.findViewById<Button>(R.id.deleteAccountButton)

        logoutButton.setOnClickListener {
            // Reemplazar el fragmento actual con LogInFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LogInFragment())
                .commit()
        }

        deleteAccountButton.setOnClickListener {
            // Reemplazar el fragmento actual con SignInFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignInFragment())
                .addToBackStack(null) // Permitir volver atrás con el botón de retroceso
                .commit()
        }

        // Referencia al botón de Analytics
        val analyticsButton = view.findViewById<View>(R.id.analyticsButton)
        analyticsButton.setOnClickListener {
            // Navegar al AnalyticsFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AnalyticsFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
