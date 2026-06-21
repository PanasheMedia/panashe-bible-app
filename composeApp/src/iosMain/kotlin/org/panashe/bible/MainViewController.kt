package org.panashe.bible

import androidx.compose.ui.window.ComposeUIViewController
import org.panashe.bible.platform.AppSettings
import platform.Foundation.NSHomeDirectory

fun MainViewController() = ComposeUIViewController {
    val homeDir = NSHomeDirectory() as String
    val settingsPath = "$homeDir/Documents/app-settings.json"
    val appSettings = AppSettings(settingsPath)

    PanasheApp(appSettings = appSettings)
}
