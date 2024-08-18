import org.gradle.kotlin.dsl.npm
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }

        binaries.executable()
    }

    sourceSets {
        
        commonMain.dependencies {

        }

        jsMain.dependencies {
            implementation(compose.runtime)

            implementation(compose.html.core)
            implementation(libs.silk.widgets)

            implementation(libs.kotlinx.serialization)

            implementation(libs.kotlinx.coroutines.core)

            implementation(npm("@stellar/freighter-api", "1.7.1"))



            implementation(projects.shared)
        }
    }
}


