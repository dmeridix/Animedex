package com.example.m7animedex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.m7animedex.data.model.Anime
import com.example.m7animedex.data.model.Fav

class FavAnimeAdapter(
    private var animeList: MutableList<Anime> = mutableListOf(),
    private var favList: MutableList<Fav> = mutableListOf(),
    private val onAnimeClickListener: OnAnimeClickListener // Usamos la interfaz aquí
) : RecyclerView.Adapter<FavAnimeAdapter.FavAnimeHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavAnimeHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.elementllistafav, parent, false)
        return FavAnimeHolder(view, onAnimeClickListener)
    }

    override fun getItemCount(): Int = animeList.size

    override fun onBindViewHolder(holder: FavAnimeHolder, position: Int) {
        holder.bind(animeList[position], favList)
    }

    fun updateList(newAnimeList: List<Anime>, newFavList: List<Fav>) {
        animeList = newAnimeList.toMutableList()
        favList = newFavList.toMutableList()
        notifyDataSetChanged()
    }


    class FavAnimeHolder(itemView: View, private val onAnimeClickListener: OnAnimeClickListener) :
        RecyclerView.ViewHolder(itemView) {

        private val titulo: TextView = itemView.findViewById(R.id.animeNombre)
        private val episodios: TextView = itemView.findViewById(R.id.animeEpisodios)
        private val tstatus: TextView = itemView.findViewById(R.id.animeEstado)
        private val imagen: ImageView = itemView.findViewById(R.id.animeImagen)

        fun bind(anime: Anime, favList: List<Fav>) {
            titulo.text = anime.title
            episodios.text = "${anime.num_episodes} episodios"

            val favorite = favList.find { it.idAnime == anime.id }
            tstatus.text = when (favorite?.status) {
                "Planned" -> "Planned"
                "Watching" -> "Watching"
                "Watched" -> "Watched"
                else -> "No Favorito"
            }

            Glide.with(itemView.context)
                .load(anime.main_picture)
                .into(imagen)

            itemView.setOnClickListener {
                onAnimeClickListener.onAnimeClick(anime) // Notificar el evento a través de la interfaz
            }
        }
    }
}
