package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "precificadoce_settings")

enum class ThemeMode(val label: String) {
    SYSTEM("Automático"),
    LIGHT("Claro"),
    DARK("Escuro");

    companion object {
        fun fromName(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

data class BusinessSettings(
    val businessName: String = "Minha Confeitaria",
    val hourlyRate: Double = 25.0,
    val defaultOverheadPercent: Double = 15.0,
    val defaultProfitMarkupPercent: Double = 100.0,
    val defaultCardFeePercent: Double = 4.99,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class BusinessSettingsRepository(private val context: Context) {
    private object Keys {
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val HOURLY_RATE = doublePreferencesKey("hourly_rate")
        val DEFAULT_OVERHEAD_PCT = doublePreferencesKey("default_overhead_pct")
        val DEFAULT_PROFIT_MARKUP_PCT = doublePreferencesKey("default_profit_markup_pct")
        val DEFAULT_CARD_FEE_PCT = doublePreferencesKey("default_card_fee_pct")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val settingsFlow: Flow<BusinessSettings> = context.dataStore.data.map { prefs ->
        BusinessSettings(
            businessName = prefs[Keys.BUSINESS_NAME] ?: "Minha Confeitaria",
            hourlyRate = prefs[Keys.HOURLY_RATE] ?: 25.0,
            defaultOverheadPercent = prefs[Keys.DEFAULT_OVERHEAD_PCT] ?: 15.0,
            defaultProfitMarkupPercent = prefs[Keys.DEFAULT_PROFIT_MARKUP_PCT] ?: 100.0,
            defaultCardFeePercent = prefs[Keys.DEFAULT_CARD_FEE_PCT] ?: 4.99,
            themeMode = ThemeMode.fromName(prefs[Keys.THEME_MODE])
        )
    }

    suspend fun updateSettings(settings: BusinessSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BUSINESS_NAME] = settings.businessName.ifBlank { "Minha Confeitaria" }
            prefs[Keys.HOURLY_RATE] = settings.hourlyRate.coerceAtLeast(0.0)
            prefs[Keys.DEFAULT_OVERHEAD_PCT] = settings.defaultOverheadPercent.coerceIn(0.0, 200.0)
            prefs[Keys.DEFAULT_PROFIT_MARKUP_PCT] = settings.defaultProfitMarkupPercent.coerceIn(0.0, 500.0)
            prefs[Keys.DEFAULT_CARD_FEE_PCT] = settings.defaultCardFeePercent.coerceIn(0.0, 50.0)
            prefs[Keys.THEME_MODE] = settings.themeMode.name
        }
    }
}
