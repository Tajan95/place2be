package de.place2be.feature.onboarding

import android.content.Context
import android.content.SharedPreferences

/**
 * Kleine abstrahierte Ablage fuer den einmaligen Onboarding-Abschluss.
 *
 * Der Screen und sein ViewModel bleiben dadurch ohne direkten Context-Zugriff
 * testbar. Die Android-Implementierung verwendet bewusst nur app-interne
 * SharedPreferences und speichert keinerlei personenbezogene Daten.
 */
interface OnboardingCompletionStore {
    fun isCompleted(): Boolean

    fun markCompleted()
}

class SharedPreferencesOnboardingCompletionStore private constructor(
    private val preferences: SharedPreferences,
) : OnboardingCompletionStore {
    override fun isCompleted(): Boolean = preferences.getBoolean(KEY_COMPLETED, false)

    override fun markCompleted() {
        preferences.edit().putBoolean(KEY_COMPLETED, true).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "place2be_onboarding"
        private const val KEY_COMPLETED = "completed"

        fun create(context: Context): SharedPreferencesOnboardingCompletionStore {
            val appContext = context.applicationContext
            return SharedPreferencesOnboardingCompletionStore(
                preferences = appContext.getSharedPreferences(
                    PREFERENCES_NAME,
                    Context.MODE_PRIVATE,
                ),
            )
        }
    }
}
