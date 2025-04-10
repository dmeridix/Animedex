// data/model/AnimeGenre.kt
package com.example.m7animedex.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeGenre(
    val animeId: Int,
    val genreId: Int
) : Parcelable