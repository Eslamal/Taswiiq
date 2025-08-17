package com.example.taswiiq.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {

    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            return context.createConfigurationContext(config)
        } else {
            config.locale = locale
            config.setLayoutDirection(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }

    fun getLanguageCode(language: String): String {
        return when (language) {
            "English" -> "en"
            "العربية" -> "ar"
            else -> Locale.getDefault().language
        }
    }
}
