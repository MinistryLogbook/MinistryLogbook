package app.ministrylogbook.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.ministrylogbook.R
import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

private val Context.dataStore by preferencesDataStore(SettingsService.Name)

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

@Serializable
data class Settings(
    val role: Role,
    val startOfPioneering: LocalDate?,
    val name: String,
    val design: Design,
    val precisionMode: Boolean,
    val sendReportReminder: Boolean
)

class SettingsService(val context: Context) {
    companion object {
        const val Name = "settings"

        private val RoleKey = stringPreferencesKey("role")
        private val StartOfPioneeringKey = stringPreferencesKey("start_of_pioneering")
        private val NameKey = stringPreferencesKey("name")
        private val DesignKey = stringPreferencesKey("design")
        private val PrecisionModeKey = booleanPreferencesKey("precision_mode")
        private val SendReportReminderKey = booleanPreferencesKey("send_report_reminder")
        private val LastBackupMillisKey = longPreferencesKey("last_backup_millis")
        private val IntroShownKey = booleanPreferencesKey("intro_shown")
    }

    val role = context.dataStore.data.map {
        it[RoleKey]?.let { role -> Role.valueOf(role) } ?: Role.Publisher
    }
    val startOfPioneering =
        context.dataStore.data.map {
            it[StartOfPioneeringKey]?.let { dateStr ->
                val date = LocalDate.parse(dateStr)
                LocalDate(date.year, date.month, 1)
            }
        }
    val roleGoal = role.map { it.goal }
    val name = context.dataStore.data.map { it[NameKey] ?: "" }
    val design = context.dataStore.data.map {
        val value = it[DesignKey]

        if (value != null) {
            Design.valueOf(value)
        } else {
            Design.System
        }
    }
    val precisionMode = context.dataStore.data.map { it[PrecisionModeKey] ?: false }
    val sendReportReminder = context.dataStore.data.map { it[SendReportReminderKey] ?: true }
    val lastBackup = context.dataStore.data.map {
        val lastBackupMillis = it[LastBackupMillisKey] ?: return@map null
        Instant.fromEpochMilliseconds(lastBackupMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    }
    val introShown = context.dataStore.data.map { it[IntroShownKey] ?: false }

    suspend fun setIntroShown() = context.dataStore.edit {
        it[IntroShownKey] = true
    }

    suspend fun setPioneerSince(date: LocalDate?) = context.dataStore.edit {
        if (date == null) {
            it.remove(StartOfPioneeringKey)
            return@edit
        }
        it[StartOfPioneeringKey] = date.toString()
    }

    suspend fun setLastBackup(dateTime: LocalDateTime?) = context.dataStore.edit {
        if (dateTime == null) {
            it.remove(LastBackupMillisKey)
            return@edit
        }
        it[LastBackupMillisKey] = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    suspend fun setRole(role: Role) = context.dataStore.edit {
        it[RoleKey] = role.name
    }

    suspend fun setName(name: String) = context.dataStore.edit {
        it[NameKey] = name
    }

    suspend fun setDesign(design: Design) = context.dataStore.edit {
        it[DesignKey] = design.name
    }

    suspend fun setPrecisionMode(precisionMode: Boolean) = context.dataStore.edit {
        it[PrecisionModeKey] = precisionMode
    }

    suspend fun setSendReportReminders(value: Boolean) = context.dataStore.edit {
        it[SendReportReminderKey] = value
    }

    suspend fun toYaml(): String {
        val settings = Settings(
            role = role.first(),
            startOfPioneering = startOfPioneering.first(),
            name = name.first(),
            design = design.first(),
            precisionMode = precisionMode.first(),
            sendReportReminder = sendReportReminder.first()
        )
        return Yaml.default.encodeToString(settings)
    }

    suspend fun fromYaml(yaml: String) {
        val data = Yaml.default.decodeFromString<HashMap<String, String>>(yaml)
        data.forEach { (key, value) ->
            when (key) {
                RoleKey.name -> setRole(Role.valueOf(value))
                StartOfPioneeringKey.name -> setPioneerSince(LocalDate.parse(value))
                NameKey.name -> setName(value)
                DesignKey.name -> setDesign(Design.valueOf(value))
                PrecisionModeKey.name -> setPrecisionMode(value.toBoolean())
                SendReportReminderKey.name -> setSendReportReminders(value.toBoolean())
            }
        }
    }
}

const val PublisherGoal = 1
const val AuxiliaryPioneerGoal = 30
const val RegularPioneerGoal = 50
const val SpecialPioneerGoal = 100
