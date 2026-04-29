package app.ministrylogbook.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.ministrylogbook.R
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private val Context.dataStore by preferencesDataStore(SettingsService.NAME)

enum class Role {
    Publisher,
    AuxiliaryPioneer,
    RegularPioneer,
    SpecialPioneer;

    val canHaveCredit: Boolean
        get() = this == RegularPioneer || this == SpecialPioneer

    val goal: Int?
        get() = when (this) {
            AuxiliaryPioneer -> AUXILIARY_PIONEER_GOAL
            RegularPioneer -> REGULAR_PIONEER_GOAL
            SpecialPioneer -> SPECIAL_PIONEER_GOAL
            else -> null
        }

    @Composable
    @ReadOnlyComposable
    fun translate(): String = when (this@Role) {
        Publisher -> stringResource(R.string.publisher)
        AuxiliaryPioneer -> stringResource(R.string.auxiliary_pioneer)
        RegularPioneer -> stringResource(R.string.regular_pioneer)
        SpecialPioneer -> stringResource(R.string.special_pioneer)
    }
}

enum class Design {
    System,
    Light,
    Dark;

    fun apply() = when (this) {
        System -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        Light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    @Composable
    @ReadOnlyComposable
    fun translate(): String = when (this@Design) {
        System -> stringResource(R.string.system_default)
        Light -> stringResource(R.string.light)
        Dark -> stringResource(R.string.dark)
    }
}

interface EntryDetailsSettings {
    val role: Flow<Role>
    val precisionMode: Flow<Boolean>
}

interface HomeSettings {
    val role: Flow<Role>
    val pioneerSince: Flow<LocalDate?>
    val roleGoal: Flow<Int?>
}

interface UserSettings {
    val role: Flow<Role>
    val pioneerSince: Flow<LocalDate?>
    val roleGoal: Flow<Int?>
    val name: Flow<String>
    val design: Flow<Design>
    val useSystemColors: Flow<Boolean>
    val precisionMode: Flow<Boolean>
    val sendReportReminder: Flow<Boolean>
    val lastBackup: Flow<LocalDateTime?>
    val introShown: Flow<Boolean>

    suspend fun setIntroShown()
    suspend fun setPioneerSince(date: LocalDate?)
    suspend fun setLastBackup(dateTime: LocalDateTime?)
    suspend fun setRole(role: Role)
    suspend fun setName(name: String)
    suspend fun setDesign(design: Design)
    suspend fun setUseSystemColors(value: Boolean)
    suspend fun setPrecisionMode(precisionMode: Boolean)
    suspend fun setSendReportReminders(value: Boolean)
}

class SettingsService(val context: Context) :
    EntryDetailsSettings,
    HomeSettings,
    UserSettings {
    companion object {
        const val NAME = "settings"

        private val RoleKey = stringPreferencesKey("role")
        private val StartOfPioneeringKey = stringPreferencesKey("start_of_pioneering")
        private val NameKey = stringPreferencesKey("name")
        private val DesignKey = stringPreferencesKey("design")
        private val UseSystemColors = booleanPreferencesKey("use_system_colors")
        private val PrecisionModeKey = booleanPreferencesKey("precision_mode")
        private val SendReportReminderKey = booleanPreferencesKey("send_report_reminder")
        private val LastBackupMillisKey = longPreferencesKey("last_backup_millis")
        private val IntroShownKey = booleanPreferencesKey("intro_shown")
    }

    override val role = context.dataStore.data.map {
        it[RoleKey]?.let { role -> Role.valueOf(role) } ?: Role.Publisher
    }.distinctUntilChanged()
    override val pioneerSince = context.dataStore.data.map {
        it[StartOfPioneeringKey]?.let { dateStr ->
            val date = LocalDate.parse(dateStr)
            LocalDate(date.year, date.month, 1)
        }
    }.distinctUntilChanged()
    override val roleGoal = role.map { it.goal }
    override val name = context.dataStore.data.map { it[NameKey] ?: "" }.distinctUntilChanged()
    override val design = context.dataStore.data.map {
        val value = it[DesignKey]

        if (value != null) {
            Design.valueOf(value)
        } else {
            Design.System
        }
    }.distinctUntilChanged()
    override val useSystemColors = context.dataStore.data.map {
        it[UseSystemColors] ?: false
    }.distinctUntilChanged()
    override val precisionMode = context.dataStore.data.map { it[PrecisionModeKey] ?: false }.distinctUntilChanged()
    override val sendReportReminder = context.dataStore.data.map {
        it[SendReportReminderKey] ?: true
    }.distinctUntilChanged()
    override val lastBackup = context.dataStore.data.map {
        val lastBackupMillis = it[LastBackupMillisKey] ?: return@map null
        Instant.fromEpochMilliseconds(lastBackupMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    }.distinctUntilChanged()
    override val introShown = context.dataStore.data.map { it[IntroShownKey] ?: false }.distinctUntilChanged()

    override suspend fun setIntroShown() {
        context.dataStore.edit { it[IntroShownKey] = true }
    }

    override suspend fun setPioneerSince(date: LocalDate?) {
        context.dataStore.edit {
            if (date == null) {
                it.remove(StartOfPioneeringKey)
                return@edit
            }
            it[StartOfPioneeringKey] = date.toString()
        }
    }

    override suspend fun setLastBackup(dateTime: LocalDateTime?) {
        context.dataStore.edit {
            if (dateTime == null) {
                it.remove(LastBackupMillisKey)
                return@edit
            }
            it[LastBackupMillisKey] = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        }
    }

    override suspend fun setRole(role: Role) {
        context.dataStore.edit { it[RoleKey] = role.name }
    }

    override suspend fun setName(name: String) {
        context.dataStore.edit { it[NameKey] = name }
    }

    override suspend fun setDesign(design: Design) {
        context.dataStore.edit { it[DesignKey] = design.name }
    }

    override suspend fun setUseSystemColors(value: Boolean) {
        context.dataStore.edit { it[UseSystemColors] = value }
    }

    override suspend fun setPrecisionMode(precisionMode: Boolean) {
        context.dataStore.edit { it[PrecisionModeKey] = precisionMode }
    }

    override suspend fun setSendReportReminders(value: Boolean) {
        context.dataStore.edit { it[SendReportReminderKey] = value }
    }
}

const val AUXILIARY_PIONEER_GOAL = 30
const val REGULAR_PIONEER_GOAL = 50
const val SPECIAL_PIONEER_GOAL = 100
