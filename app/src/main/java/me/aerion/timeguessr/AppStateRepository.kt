package me.aerion.timeguessr

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timeguessr_preferences")

class AppStateRepository(private val context: Context) {
    private val gson = Gson()

    private object PreferencesKeys {
        val ROUNDS = stringPreferencesKey("rounds")
        val ROUND_RESULTS = stringPreferencesKey("round_results")
        val CURRENT_ROUND_INDEX = intPreferencesKey("current_round_index")
        val CURRENT_PAGE = stringPreferencesKey("current_page")
        val LATEST_ROUND_NUMBER = intPreferencesKey("latest_round_number")
    }

    suspend fun saveAppState(
        rounds: List<RoundData>?,
        roundResults: List<RoundResult>,
        currentRoundIndex: Int,
        currentPage: Page
    ) {
        context.dataStore.edit { preferences ->
            rounds?.let {
                preferences[PreferencesKeys.ROUNDS] = gson.toJson(it)
                preferences[PreferencesKeys.LATEST_ROUND_NUMBER] = it.firstOrNull()?.No?.toInt() ?: 0
            }
            preferences[PreferencesKeys.ROUND_RESULTS] = gson.toJson(roundResults)
            preferences[PreferencesKeys.CURRENT_ROUND_INDEX] = currentRoundIndex
            preferences[PreferencesKeys.CURRENT_PAGE] = currentPage.name
        }
    }

    suspend fun getSavedRounds(): List<RoundData>? {
        val roundsJson = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ROUNDS]
        }.firstOrNull()

        return if (roundsJson != null) {
            val type = object : TypeToken<List<RoundData>>() {}.type
            gson.fromJson(roundsJson, type)
        } else {
            null
        }
    }

    suspend fun getSavedRoundResults(): List<RoundResult> {
        val resultsJson = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ROUND_RESULTS]
        }.firstOrNull()

        return if (resultsJson != null) {
            val type = object : TypeToken<List<RoundResult>>() {}.type
            gson.fromJson(resultsJson, type)
        } else {
            emptyList()
        }
    }

    suspend fun getSavedCurrentRoundIndex(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.CURRENT_ROUND_INDEX] ?: 0
        }.firstOrNull() ?: 0
    }

    suspend fun getSavedCurrentPage(): Page {
        val pageName = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.CURRENT_PAGE]
        }.firstOrNull()

        return try {
            if (pageName != null) Page.valueOf(pageName) else Page.RoundPlayPage
        } catch (e: Exception) {
            Page.RoundPlayPage
        }
    }
}