package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sb_writer_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("app_theme") // "dark", "light", "sepia", "system"
        val KEY_FONT_FAMILY = stringPreferencesKey("editor_font_family") // "serif", "sans", "mono"
        val KEY_FONT_SIZE = floatPreferencesKey("editor_font_size") // default 17f
        val KEY_LINE_SPACING = floatPreferencesKey("editor_line_spacing") // default 1.5f
        val KEY_MARGIN_DP = intPreferencesKey("editor_margin_dp") // default 16
        val KEY_TEXT_ALIGN = stringPreferencesKey("editor_text_align") // "left", "justify", "center"
        val KEY_DAILY_GOAL = intPreferencesKey("daily_word_goal") // default 1000
        val KEY_AI_PERSONA = stringPreferencesKey("sb_ai_persona") // "editor", "lucu", "profesional"
        val KEY_AI_ICON_X = floatPreferencesKey("sb_ai_icon_x")
        val KEY_AI_ICON_Y = floatPreferencesKey("sb_ai_icon_y")
        val KEY_PIN_ENABLED = booleanPreferencesKey("pin_lock_enabled")
        val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        val KEY_VIEW_MODE = stringPreferencesKey("library_view_mode") // "grid", "list"
        val KEY_CUSTOM_API_KEY_1 = stringPreferencesKey("custom_api_key_1")
        val KEY_CUSTOM_API_KEY_2 = stringPreferencesKey("custom_api_key_2")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "dark" }
    val fontFamilyFlow: Flow<String> = context.dataStore.data.map { it[KEY_FONT_FAMILY] ?: "serif" }
    val fontSizeFlow: Flow<Float> = context.dataStore.data.map { it[KEY_FONT_SIZE] ?: 17f }
    val lineSpacingFlow: Flow<Float> = context.dataStore.data.map { it[KEY_LINE_SPACING] ?: 1.5f }
    val marginDpFlow: Flow<Int> = context.dataStore.data.map { it[KEY_MARGIN_DP] ?: 16 }
    val textAlignFlow: Flow<String> = context.dataStore.data.map { it[KEY_TEXT_ALIGN] ?: "left" }
    val dailyGoalFlow: Flow<Int> = context.dataStore.data.map { it[KEY_DAILY_GOAL] ?: 1000 }
    val aiPersonaFlow: Flow<String> = context.dataStore.data.map { it[KEY_AI_PERSONA] ?: "editor" }
    val aiIconXFlow: Flow<Float> = context.dataStore.data.map { it[KEY_AI_ICON_X] ?: -1f }
    val aiIconYFlow: Flow<Float> = context.dataStore.data.map { it[KEY_AI_ICON_Y] ?: -1f }
    val pinEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_PIN_ENABLED] ?: false }
    val pinHashFlow: Flow<String> = context.dataStore.data.map { it[KEY_PIN_HASH] ?: "" }
    val viewModeFlow: Flow<String> = context.dataStore.data.map { it[KEY_VIEW_MODE] ?: "grid" }
    val customApiKey1Flow: Flow<String> = context.dataStore.data.map { it[KEY_CUSTOM_API_KEY_1] ?: "" }
    val customApiKey2Flow: Flow<String> = context.dataStore.data.map { it[KEY_CUSTOM_API_KEY_2] ?: "" }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun setFontFamily(font: String) {
        context.dataStore.edit { it[KEY_FONT_FAMILY] = font }
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { it[KEY_FONT_SIZE] = size }
    }

    suspend fun setLineSpacing(spacing: Float) {
        context.dataStore.edit { it[KEY_LINE_SPACING] = spacing }
    }

    suspend fun setMarginDp(margin: Int) {
        context.dataStore.edit { it[KEY_MARGIN_DP] = margin }
    }

    suspend fun setTextAlign(align: String) {
        context.dataStore.edit { it[KEY_TEXT_ALIGN] = align }
    }

    suspend fun setDailyGoal(goal: Int) {
        context.dataStore.edit { it[KEY_DAILY_GOAL] = goal }
    }

    suspend fun setAiPersona(persona: String) {
        context.dataStore.edit { it[KEY_AI_PERSONA] = persona }
    }

    suspend fun setAiIconPosition(x: Float, y: Float) {
        context.dataStore.edit {
            it[KEY_AI_ICON_X] = x
            it[KEY_AI_ICON_Y] = y
        }
    }

    suspend fun setPin(pin: String) {
        context.dataStore.edit {
            it[KEY_PIN_HASH] = pin
            it[KEY_PIN_ENABLED] = pin.isNotEmpty()
        }
    }

    suspend fun setPinEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_PIN_ENABLED] = enabled }
    }

    suspend fun setViewMode(mode: String) {
        context.dataStore.edit { it[KEY_VIEW_MODE] = mode }
    }

    suspend fun setCustomApiKeys(key1: String, key2: String) {
        context.dataStore.edit {
            it[KEY_CUSTOM_API_KEY_1] = key1
            it[KEY_CUSTOM_API_KEY_2] = key2
        }
    }
}
