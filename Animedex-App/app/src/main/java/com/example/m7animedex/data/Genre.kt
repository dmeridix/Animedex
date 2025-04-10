// data/model/Genre.kt
package com.example.m7animedex.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Genre(
    val id: Int,
    val name: String
) : Parcelable