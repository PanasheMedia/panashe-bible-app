package org.panashe.bible

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.panashe.bible.features.audio.createTtsEngine
import org.panashe.bible.platform.AppSettings

/** Android host for the shared Compose UI. Mirrors iosApp's MainViewController. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsPath = "${filesDir.absolutePath}/app-settings.json"
        val appSettings = AppSettings(settingsPath)
        // Pre-warm TTS engine
        createTtsEngine(applicationContext)
        setContent {
            PanasheApp(appSettings = appSettings)
        }
    }
}
