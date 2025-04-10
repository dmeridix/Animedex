package com.example.m7animedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class FragmentHeader : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_header, container, false)

        val avatarIcon = view.findViewById<ImageView>(R.id.avatar_icon)
        val logoImage = view.findViewById<ImageView>(R.id.imageView4)
        val headerTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)

        avatarIcon.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        val navigateToHome = View.OnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        logoImage.setOnClickListener(navigateToHome)
        headerTitle.setOnClickListener(navigateToHome)

        return view
    }
}
