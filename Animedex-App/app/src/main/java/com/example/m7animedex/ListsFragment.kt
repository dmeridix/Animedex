package com.example.m7animedex

import AnimeDetailFragment
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.m7animedex.data.AnimeAPI
import com.example.m7animedex.data.api.AnimeService
import com.example.m7animedex.data.model.Anime
import com.example.m7animedex.data.model.Fav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ListsFragment : Fragment(), OnAnimeClickListener {

    // Vistes i components del layout
    private lateinit var searchEditText: EditText
    private lateinit var buttonPlanned: Button
    private lateinit var buttonWatching: Button
    private lateinit var buttonWatched: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var favAnimeAdapter: FavAnimeAdapter

    // Servei per accedir a l'API d'Anime
    private val animeService: AnimeService = AnimeAPI.getService()

    // Variables per rastrejar l'estat actual i la consulta de cerca
    private var currentStatus: String = "Planned"
    private var currentQuery: String = ""

    /**
     * Infla el layout del fragment i inicialitza les vistes i funcionalitats.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_lists, container, false)

        // Inicialitzar les vistes
        searchEditText = view.findViewById(R.id.searchBox)
        buttonPlanned = view.findViewById(R.id.buttonPlanned)
        buttonWatching = view.findViewById(R.id.buttonWatching)
        buttonWatched = view.findViewById(R.id.buttonWatched)
        recyclerView = view.findViewById(R.id.recyclerViewLists)

        // Configurar el RecyclerView amb un adaptador personalitzat
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        favAnimeAdapter = FavAnimeAdapter(
            mutableListOf(), // Llista inicial d'animes (buida)
            mutableListOf(), // Llista inicial de favorits (buida)
            this             // Instancia de OnAnimeClickListener
        )
        recyclerView.adapter = favAnimeAdapter

        // Carregar dades inicials
        loadFilteredData()

        // Configura els botons de filtre (Planned, Watching, Watched).
        buttonPlanned.setOnClickListener {
            currentStatus = "Planned"
            loadFilteredData()
        }
        buttonWatching.setOnClickListener {
            currentStatus = "Watching"
            loadFilteredData()
        }
        buttonWatched.setOnClickListener {
            currentStatus = "Watched"
            loadFilteredData()
        }

        // Configurar el buscador
        setupSearchBox()

        return view
    }

    /**
     * Configura el camp de cerca per cercar animes favorits per títol.
     */
    private fun setupSearchBox() {
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == KeyEvent.KEYCODE_ENTER || actionId == KeyEvent.ACTION_DOWN) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    // Si hi ha una consulta, actualitzar la variable i carregar dades filtrades
                    currentQuery = query
                    loadFilteredData()
                } else {
                    // Si el camp està buit, netejar la consulta i carregar tots els favorits
                    currentQuery = ""
                    loadFilteredData()
                }
                true
            } else {
                false
            }
        }
    }

    /**
     * Carrega les dades filtrades segons l'estat actual i la consulta de cerca.
     */
    private fun loadFilteredData() {
        if (currentQuery.isNotEmpty()) {
            // Si hi ha una consulta, cercar favorits per títol i estat
            searchFavorites(currentQuery, currentStatus)
        } else {
            // Si no hi ha consulta, carregar tots els favorits de l'estat actual
            loadFavoritesByStatus(currentStatus)
        }
    }

    /**
     * Cerca favorits per títol i estat utilitzant l'endpoint /favorites/search.
     */
    private fun searchFavorites(query: String, status: String) {
        lifecycleScope.launch {
            try {
                val response = animeService.searchFavorites(query)
                if (response.isSuccessful) {
                    val favorites = response.body() ?: emptyList()
                    // Filtrar els favorits per estat
                    val filteredFavorites = favorites.filter { it.status.equals(status, ignoreCase = true) }
                    val animeList = getAnimeDetails(filteredFavorites)
                    withContext(Dispatchers.Main) {
                        favAnimeAdapter.updateList(animeList, filteredFavorites)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error al buscar favoritos", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Carga los favoritos según el estado seleccionado (Planned, Watching, Completed).
     */
    private fun loadFavoritesByStatus(status: String) {
        lifecycleScope.launch {
            try {
                val response = when (status) {
                    "Planned" -> animeService.getPlannedFavorites()
                    "Watching" -> animeService.getWatchingFavorites()
                    "Watched" -> animeService.getCompletedFavorites() // Cambiado a Watched
                    else -> null
                }

                if (response != null && response.isSuccessful) {
                    val favorites = response.body() ?: emptyList()
                    val animeList = getAnimeDetails(favorites)
                    withContext(Dispatchers.Main) {
                        favAnimeAdapter.updateList(animeList, favorites)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Obtiene los detalles de los animes asociados a los favoritos.
     */
    private suspend fun getAnimeDetails(favorites: List<Fav>): List<Anime> {
        val animeList = mutableListOf<Anime>()
        for (fav in favorites) {
            try {
                println("📡 Solicitando anime con ID: ${fav.idAnime}")
                val response = animeService.getAnimeById(fav.idAnime)
                if (response.isSuccessful) {
                    response.body()?.let {
                        println("Anime recibido: ${it.title}")
                        animeList.add(it)
                    }
                } else {
                    println("Error en getAnimeById(${fav.idAnime}): ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Error en getAnimeById(${fav.idAnime}): ${e.message}")
            }
        }
        return animeList
    }

    /**
     * Abre el fragmento de detalles del anime seleccionado.
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

    override fun onAnimeClick(anime: Anime) {
        openAnimeDetailFragment(anime)
    }
}
