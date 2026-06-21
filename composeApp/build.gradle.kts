import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.util.Properties
import java.io.FileInputStream

val xcconfig = Properties()
val xcconfigFile = rootProject.file("iosApp/Configuration/Version.xcconfig")
if (xcconfigFile.exists()) {
    xcconfig.load(FileInputStream(xcconfigFile))
}
val appVersionName = xcconfig.getProperty("MARKETING_VERSION") ?: "1.0.0"
val appVersionCode = xcconfig.getProperty("CURRENT_PROJECT_VERSION")?.toIntOrNull() ?: 1

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("androidx.compose.foundation.layout.ExperimentalLayoutApi")
    }

    wasmJs {
        moduleName = "panashe-bible"
        browser {
            commonWebpackConfig {
                outputFileName = "panashe-bible.js"
            }
        }
        binaries.executable()
    }

    val xcf = XCFramework("PanasheBibleShared")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "PanasheBibleShared"
            isStatic = true
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}
