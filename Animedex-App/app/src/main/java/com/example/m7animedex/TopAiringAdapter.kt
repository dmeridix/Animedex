package com.example.m7animedex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.m7animedex.data.model.Anime
class TopAiringAdapter(private var animeList: List<Anime>,
                       private val onItemClickListener: (Anime) -> Unit
) :
    RecyclerView.Adapter<TopAiringAdapter.AnimeViewHolder>() {

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImagen: ImageView = itemView.findViewById(R.id.animeImagen)
        val animeNombre: TextView = itemView.findViewById(R.id.animeNombre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.elementllistahome, parent, false)
        return AnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val anime = animeList[position]
        holder.animeNombre.text = anime.title

        Glide.with(holder.itemView.context)
            .load(anime.main_picture)
            .into(holder.animeImagen)

        // Configurar el click listener
        holder.itemView.setOnClickListener {
            onItemClickListener(anime) // Pasar el objeto Anime al listener
        }
    }

    override fun getItemCount(): Int = animeList.size

    fun updateList(newList: List<Anime>) {
        animeList = newList
        notifyDataSetChanged()
    }
}
