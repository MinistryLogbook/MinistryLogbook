package com.github.danieldaeschle.ministrynotes.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

enum class Role {
    Publisher, AuxiliaryPioneer, RegularPioneer, SpecialPioneer;

    val canHaveCredit: Boolean
        get() = this == RegularPioneer || this == SpecialPioneer

    @Composable
    fun translate(): String {
//        val context = LocalContext.current

        return when (this@Role) {
            Publisher -> "Publisher"
            AuxiliaryPioneer -> "Auxiliary Pioneer"
            RegularPioneer -> "Regular Pioneer"
            SpecialPioneer -> "Special Pioneer"
        }
    }
}

class SettingsDataStore(val context: Context) {
    companion object {
        private val GOAL_KEY = intPreferencesKey("goal")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val FIRST_NAME_KEY = stringPreferencesKey("first_name")
        private val LAST_NAME_KEY = stringPreferencesKey("last_name")
    }

    val role = context.dataStore.data.map {
        it[ROLE_KEY]?.let { it1 -> Role.valueOf(it1) } ?: Role.Publisher
    }
    val goal = context.dataStore.data.map { it[GOAL_KEY] }.combine(role) { g, r ->
        if (g != null) {
            return@combine g
        }
        return@combine when (r) {
            Role.Publisher -> null
            Role.AuxiliaryPioneer -> AuxiliaryPioneerGoal
            Role.RegularPioneer -> RegularPioneerGoal
            Role.SpecialPioneer -> SpecialPioneerGoal
        }
    }
    val manuallySetGoal = context.dataStore.data.map { it[GOAL_KEY] }
    val firstName = context.dataStore.data.map { it[FIRST_NAME_KEY] }
    val lastName = context.dataStore.data.map { it[LAST_NAME_KEY] }

    suspend fun setRole(role: Role) = context.dataStore.edit {
        it[ROLE_KEY] = role.toString()
    }

    suspend fun setGoal(goal: Int) = context.dataStore.edit {
        it[GOAL_KEY] = goal
    }

    suspend fun resetGoal() = context.dataStore.edit {
        it.remove(GOAL_KEY)
    }
}

@Composable
fun rememberSettingsDataStore(): SettingsDataStore {
    val context = LocalContext.current
    return remember { SettingsDataStore(context) }
}

private const val AuxiliaryPioneerGoal = 30
private const val RegularPioneerGoal = 50
private const val SpecialPioneerGoal = 100