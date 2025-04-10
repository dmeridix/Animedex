// data/model/Anime.kt
package com.example.m7animedex.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Anime(
    val id: Int,
    val title: String,
    val main_picture: String?,  // Opcional
    val start_date: String?,        // Opcional
    val end_date: String?,          // Opcional
    val synopsis: String,
    val mean: Float? = null,          // Opcional
    val rank: Int? = null,            // Opcional
    val popularity: Int? = null,      // Opcional
    val media_type: String,
    val status: String,
    val num_episodes: Int? = null,     // Opcional
    val start_season: String? = null,  // Opcional
    val genres: List<Genre>           // Llista de gèneres associats
) : Parcelable