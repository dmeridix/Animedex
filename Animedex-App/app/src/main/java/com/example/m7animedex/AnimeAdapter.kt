package com.example.m7animedex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.m7animedex.data.model.Anime
class AnimeAdapter(
    private var animeList: MutableList<Anime>,
    private val onAnimeClickListener: OnAnimeClickListener // Usamos la interfaz aquí
) : RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImagenHome: ImageView = itemView.findViewById(R.id.animeImagenHome)
        val animeNombreHome: TextView = itemView.findViewById(R.id.animeNombreHome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.elementllistahome, parent, false)
        return AnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val anime = animeList[position]
        val maxLength = 20 // Define la longitud máxima del título

        // Acorta el título si es necesario
        val shortTitle = if (anime.title.length > maxLength) {
            "${anime.title.substring(0, maxLength)}..."
        } else {
            anime.title
        }

        holder.animeNombreHome.text = shortTitle // Usa el título acortado

        Glide.with(holder.itemView.context)
            .load(anime.main_picture)
            .into(holder.animeImagenHome)

        holder.itemView.setOnClickListener {
            onAnimeClickListener.onAnimeClick(anime) // Notificar el evento a través de la interfaz
        }
    }

    override fun getItemCount(): Int = animeList.size

    fun updateList(newList: List<Anime>) {
        animeList.clear()
        animeList.addAll(newList)
        notifyDataSetChanged()
    }
}