package app.ministrylogbook.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.ministrylogbook.R
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

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
    @ReadOnlyComposable
    fun translate(): String {
        val context = LocalContext.current
        return when (this@Role) {
            Publisher -> context.getString(R.string.publisher)
            AuxiliaryPioneer -> context.getString(R.string.auxiliary_pioneer)
            RegularPioneer -> context.getString(R.string.regular_pioneer)
            SpecialPioneer -> context.getString(R.string.special_pioneer)
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
    @ReadOnlyComposable
    fun translate(): String {
        val context = LocalContext.current
        return when (this@Design) {
            System -> context.getString(R.string.system_default)
            Light -> context.getString(R.string.light)
            Dark -> context.getString(R.string.dark)
        }
    }
}

class SettingsDataStore(val context: Context) {
    companion object {
        private val ROLE_KEY = stringPreferencesKey("role")
        private val PIONEER_SINCE_KEY = stringPreferencesKey("pioneer_since")
        private val NAME_KEY = stringPreferencesKey("name")
        private val DESIGN_KEY = stringPreferencesKey("design")
    }

    val role = context.dataStore.data.map {
        it[ROLE_KEY]?.let { role -> Role.valueOf(role) } ?: Role.Publisher
    }
    val pioneerSince =
        context.dataStore.data.map { it[PIONEER_SINCE_KEY]?.let { dateStr -> LocalDate.parse(dateStr) } }
    val roleGoal = role.map { it.goal }
    val name = context.dataStore.data.map { it[NAME_KEY] ?: "" }
    val design = context.dataStore.data.map {
        val value = it[DESIGN_KEY]

        if (value != null) {
            Design.valueOf(value)
        } else {
            Design.System
        }
    }

    suspend fun setPioneerSince(date: LocalDate?) = context.dataStore.edit {
        if (date == null) {
            it.remove(PIONEER_SINCE_KEY)
            return@edit
        }
        it[PIONEER_SINCE_KEY] = date.toString()
    }

    suspend fun setRole(role: Role) = context.dataStore.edit {
        it[ROLE_KEY] = role.name
    }

    suspend fun setName(name: String) = context.dataStore.edit {
        it[NAME_KEY] = name
    }

    suspend fun setDesign(design: Design) = context.dataStore.edit {
        it[DESIGN_KEY] = design.name
    }
}

const val PublisherGoal = 1
const val AuxiliaryPioneerGoal = 30
const val RegularPioneerGoal = 50
const val SpecialPioneerGoal = 100
