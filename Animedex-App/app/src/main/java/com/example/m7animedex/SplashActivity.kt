package com.example.m7animedex

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Activa la API oficial de Splash Screen antes de crear la UI
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Asegúrate de tener este layout

        val logo = findViewById<ImageView>(R.id.splash_logo)

        // Animación de escalado para hacer el logo más grande
        val scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 1f, 3f)
        val scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 1f, 3f)

        // Animación de desvanecimiento
        val fadeOut = ObjectAnimator.ofFloat(logo, "alpha", 1f, 0f)

        // Configurar duración e interpolación
        val animationSet = AnimatorSet()
        animationSet.playTogether(scaleX, scaleY, fadeOut)
        animationSet.duration = 1200
        animationSet.interpolator = AccelerateInterpolator()

        // Iniciar animación y cambiar a MainActivity después
        animationSet.start()

        logo.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // Suaviza la transición
            finish()
        }, 1300) // Un poco más de delay para que se vea bien la animación
    }
}
