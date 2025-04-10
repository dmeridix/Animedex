package com.example.m7animedex

import AnimeDetailFragment
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.m7animedex.data.AnimeAPI
import com.example.m7animedex.data.model.Anime
import com.example.m7animedex.data.model.Fav
import kotlinx.coroutines.launch

class SearchFragment : Fragment(), OnAnimeClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavAnimeAdapter
    private lateinit var searchBox: EditText
    private val animeList: MutableList<Anime> = mutableListOf()
    private val favList: MutableList<Fav> = mutableListOf() // Ahora `favList` se cargará desde la API

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchBox = view.findViewById(R.id.searchBox)
        recyclerView = view.findViewById(R.id.recyclerViewAnimes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = FavAnimeAdapter(
            animeList, // Lista de Anime
            favList,   // Lista de Favoritos
            this       // Instancia de OnAnimeClickListener (el fragmento)
        )
        recyclerView.adapter = adapter

        loadFavorites()  // Cargar favoritos y su estado (status)
        loadAllAnimes()

        searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == KeyEvent.KEYCODE_ENTER || actionId == KeyEvent.ACTION_DOWN) {
                val query = searchBox.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchAnimes(query)
                } else {
                    loadAllAnimes()
                }
                true
            } else {
                false
            }
        }

        return view
    }

    // Método para cargar la lista de favoritos con su `status`
    private fun loadFavorites() {
        lifecycleScope.launch {
            try {
                val response = AnimeAPI.getService().getFavorites()  // Supuesta llamada a la API
                if (response.isSuccessful && response.body() != null) {
                    favList.clear()
                    favList.addAll(response.body()!!)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar favoritos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchAnimes(query: String) {
        lifecycleScope.launch {
            try {
                val response = AnimeAPI.getService().searchAnimes(query)
                if (response.isSuccessful && response.body() != null) {
                    val animes = response.body()!!
                    animeList.clear()
                    animeList.addAll(animes)
                    adapter.updateList(animes, favList)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAllAnimes() {
        lifecycleScope.launch {
            try {
                val response = AnimeAPI.getService().getRandomAnimes()
                if (response.isSuccessful && response.body() != null) {
                    val animes = response.body()!!
                    animeList.clear()
                    animeList.addAll(animes)
                    adapter.updateList(animes, favList)
                } else {
                    Toast.makeText(requireContext(), "Error al cargar animes", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openAnimeDetailFragment(anime: Anime) {
        val fragment = AnimeDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("ARG_ANIME_ID", anime.id)
                putParcelable("ARG_ANIME", anime)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onAnimeClick(anime: Anime) {
        openAnimeDetailFragment(anime)
    }
}
