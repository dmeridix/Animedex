package com.example.m7animedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class LogInFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_log_in, container, false)

        val logInButton = view.findViewById<Button>(R.id.log_in_button)
        val signInButton = view.findViewById<Button>(R.id.sign_in_button)

        logInButton.setOnClickListener {
            // Reemplazar LogInFragment con HomeFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        signInButton.setOnClickListener {
            // Reemplazar LogInFragment con SignInFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignInFragment())
                .addToBackStack(null) // Permite volver atrás con el botón de retroceso
                .commit()
        }

        return view
    }
}
