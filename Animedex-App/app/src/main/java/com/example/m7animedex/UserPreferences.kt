package com.example.m7animedex
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val ANIME_VISITS_KEY = stringPreferencesKey("anime_visits")
        private val FAVORITE_COUNT_KEY = intPreferencesKey("favorite_count")
        private val TOTAL_VISITS_KEY = intPreferencesKey("total_visits")
    }

    // Guardar que el usuario ha visitado un anime
    suspend fun registerAnimeVisit(animeId: Int) {
        context.dataStore.edit { preferences ->
            val visits = preferences[ANIME_VISITS_KEY]?.split(",")?.mapNotNull { it.toIntOrNull() }?.toMutableList() ?: mutableListOf()
            visits.add(animeId)  // Registrar la visita
            preferences[ANIME_VISITS_KEY] = visits.joinToString(",")

            // También incrementamos el contador total de visitas
            val total = (preferences[TOTAL_VISITS_KEY] ?: 0) + 1
            preferences[TOTAL_VISITS_KEY] = total
        }
    }

    // Guardar si el usuario marcó un anime como favorito
    suspend fun registerFavorite(animeId: Int, isFavorite: Boolean) {
        context.dataStore.edit { preferences ->
            if (isFavorite) {
                val count = (preferences[FAVORITE_COUNT_KEY] ?: 0) + 1
                preferences[FAVORITE_COUNT_KEY] = count
            }
        }
    }

    // Obtener cuántas veces se ha visitado cada anime
    fun getAnimeVisits(): Flow<Map<Int, Int>> {
        return context.dataStore.data.map { preferences ->
            val visits = preferences[ANIME_VISITS_KEY]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
            visits.groupingBy { it }.eachCount()
        }
    }

    // Obtener total de visitas y favoritos
    fun getStats(): Flow<Pair<Int, Int>> {
        return context.dataStore.data.map { preferences ->
            val total = preferences[TOTAL_VISITS_KEY] ?: 1  // Evita división por cero
            val favs = preferences[FAVORITE_COUNT_KEY] ?: 0
            Pair(total, favs)
        }
    }
}
