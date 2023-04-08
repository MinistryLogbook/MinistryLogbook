package com.github.danieldaeschle.ministrynotes.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

enum class Role {
    Publisher, AuxiliaryPioneer, RegularPioneer, SpecialPioneer;

    val canHaveCredit: Boolean
        get() = this == RegularPioneer || this == SpecialPioneer

    @Composable
    fun translate(): String {
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
        private val NAME_KEY = stringPreferencesKey("name")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    val role = context.dataStore.data.map {
        it[ROLE_KEY]?.let { role -> Role.valueOf(role) } ?: Role.Publisher
    }
    val goal = context.dataStore.data.map { it[GOAL_KEY] }.combine(role) { goal, role ->
        if (goal != null) {
            return@combine goal
        }
        return@combine when (role) {
            Role.Publisher -> PublisherGoal
            Role.AuxiliaryPioneer -> AuxiliaryPioneerGoal
            Role.RegularPioneer -> RegularPioneerGoal
            Role.SpecialPioneer -> SpecialPioneerGoal
        }
    }
    val manuallySetGoal = context.dataStore.data.map { it[GOAL_KEY] }
    val name = context.dataStore.data.map { it[NAME_KEY] ?: "" }
    val locale by lazy {
        AppCompatDelegate.getApplicationLocales().get(0)
    }

    suspend fun setRole(role: Role) = context.dataStore.edit {
        it[ROLE_KEY] = role.toString()
    }

    suspend fun setName(name: String) = context.dataStore.edit {
        it[NAME_KEY] = name
    }

    fun setLocale(locale: Locale?) {
        if (locale == null) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            return
        }
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
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

const val PublisherGoal = 1
const val AuxiliaryPioneerGoal = 30
const val RegularPioneerGoal = 50
const val SpecialPioneerGoal = 100