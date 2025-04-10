package com.example.m7animedex

import AnimeDetailFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.m7animedex.data.AnimeAPI
import com.example.m7animedex.data.api.AnimeService
import com.example.m7animedex.data.model.Anime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(), OnAnimeClickListener {

    // Vistes i components del layout
    private lateinit var topAiringRecyclerView: RecyclerView
    private lateinit var mostPopularRecyclerView: RecyclerView
    private lateinit var topAiringAdapter: AnimeAdapter
    private lateinit var mostPopularAdapter: AnimeAdapter

    // Servei per accedir a l'API d'Anime
    private lateinit var animeApiService: AnimeService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicialitzar vistes
        topAiringRecyclerView = view.findViewById(R.id.topAiringGrid)
        mostPopularRecyclerView = view.findViewById(R.id.mostPopularGrid)

        // Configurar els RecyclerViews amb un layout horitzontal
        topAiringRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mostPopularRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Definir la funció onItemClick per obrir el detall de l'anime
        val onItemClick: (Anime) -> Unit = { anime ->
            openAnimeDetailFragment(anime)
        }

        // Inicialitzar els adapters
        topAiringAdapter = AnimeAdapter(mutableListOf(), this)
        mostPopularAdapter = AnimeAdapter(mutableListOf(), this)

        topAiringRecyclerView.adapter = topAiringAdapter
        mostPopularRecyclerView.adapter = mostPopularAdapter

        // Inicialitzar el servei de l'API
        animeApiService = AnimeAPI.getService()

        // Carregar les dades
        fetchTopAiringAnimes()
        fetchMostPopularAnimes()
    }

    /**
     * Obre el fragment de detalls de l'anime seleccionat.
     */
    private fun openAnimeDetailFragment(anime: Anime) {
        val fragment = AnimeDetailFragment()
        val args = Bundle()
        args.putParcelable("ARG_ANIME", anime)
        fragment.arguments = args

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Obté els animes en emisió utilitzant Retrofit amb coroutines.
     */
    private fun fetchTopAiringAnimes() {
        lifecycleScope.launch {
            try {
                // Realitzar la petició a la xarxa en el fil IO
                val response = withContext(Dispatchers.IO) {
                    animeApiService.getAiringAnime()
                }

                // Actualitzar la UI en el fil principal
                if (response.isSuccessful) {
                    val animes = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        topAiringAdapter.updateList(animes)
                    }
                } else {
                    Log.e(
                        "HomeFragment",
                        "Error al obtenir animes en emisió: ${response.errorBody()?.string()}"
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error al obtenir animes en emisió",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error en la petició: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error en la xarxa", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Obté els animes més populars utilitzant Retrofit amb coroutines.
     */
    private fun fetchMostPopularAnimes() {
        lifecycleScope.launch {
            try {
                // Realitzar la petició a la xarxa en el fil IO
                val response = withContext(Dispatchers.IO) {
                    animeApiService.getPopularAnime()
                }

                // Actualitzar la UI en el fil principal
                if (response.isSuccessful) {
                    val mostPopularAnimes = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        mostPopularAdapter.updateList(mostPopularAnimes)
                    }
                } else {
                    Log.e(
                        "HomeFragment",
                        "Error al obtenir animes populars: ${response.errorBody()?.string()}"
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error al obtenir animes populars",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error en la petició: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error en la xarxa", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onAnimeClick(anime: Anime) {
        openAnimeDetailFragment(anime)
    }
}