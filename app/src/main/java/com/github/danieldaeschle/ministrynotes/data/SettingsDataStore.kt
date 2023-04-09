package com.github.danieldaeschle.ministrynotes.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

enum class Role {
    Publisher, AuxiliaryPioneer, RegularPioneer, SpecialPioneer;

    val canHaveCredit: Boolean
        get() = this == RegularPioneer || this == SpecialPioneer

    val goal: Int
        get() = when (this) {
            Publisher -> PublisherGoal
            AuxiliaryPioneer -> AuxiliaryPioneerGoal
            RegularPioneer -> RegularPioneerGoal
            SpecialPioneer -> SpecialPioneerGoal
        }

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

enum class Design {
    System, Light, Dark;

    fun apply() = when (this) {
        System -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        Light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    @Composable
    fun translate(): String {
        return when (this@Design) {
            System -> "System default"
            Light -> "Light"
            Dark -> "Dark"
        }
    }
}

class SettingsDataStore(val context: Context) {
    companion object {
        private val GOAL_KEY = intPreferencesKey("goal")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val NAME_KEY = stringPreferencesKey("name")
        private val DESIGN_KEY = stringPreferencesKey("design")
    }

    val role = context.dataStore.data.map {
        it[ROLE_KEY]?.let { role -> Role.valueOf(role) } ?: Role.Publisher
    }
    val roleGoal = role.map { it.goal }
    val manuallySetGoal = context.dataStore.data.map { it[GOAL_KEY] }
    val goal = roleGoal.combine(manuallySetGoal) { rg, msg -> msg ?: rg }
    val name = context.dataStore.data.map { it[NAME_KEY] ?: "" }
    val design = context.dataStore.data.map {
        val value = it[DESIGN_KEY]

        if (value != null) {
            Design.valueOf(value)
        } else {
            Design.System
        }
    }

    suspend fun setRole(role: Role) = manuallySetGoal.collectLatest { msg ->
        if (msg == role.goal) {
            resetGoal()
        }
        context.dataStore.edit {
            it[ROLE_KEY] = role.name
        }
    }

    suspend fun setName(name: String) = context.dataStore.edit {
        it[NAME_KEY] = name
    }

    suspend fun setDesign(design: Design) = context.dataStore.edit {
        it[DESIGN_KEY] = design.name
    }

    suspend fun setGoal(goal: Int?) = roleGoal.collectLatest { rg ->
        if (rg == goal || goal == null) {
            resetGoal()
            return@collectLatest
        }
        context.dataStore.edit {
            it[GOAL_KEY] = goal
        }
    }

    suspend fun resetGoal() = context.dataStore.edit {
        it.remove(GOAL_KEY)
    }
}

@Composable
fun rememberSettingsDataStore(): SettingsDataStore {
    val context = LocalContext.current
    return remember { context.settingsDataStore() }
}

fun Context.settingsDataStore(): SettingsDataStore {
    return SettingsDataStore(this)
}

const val PublisherGoal = 1
const val AuxiliaryPioneerGoal = 30
const val RegularPioneerGoal = 50
const val SpecialPioneerGoal = 100