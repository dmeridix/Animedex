// data/model/Fav.kt
package com.example.m7animedex.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Fav(
    val idUsuario: Int,
    @SerializedName("id_anime") val idAnime: Int,
    val status: String,
    @SerializedName("date_added") val dateAdded: String?,
    @SerializedName("date_finished") val dateFinished: String? = null,
    val main_picture: String?
) : Parcelable
