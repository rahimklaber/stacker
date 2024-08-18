import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    
}

kotlin {
    js {
        browser()

        binaries.executable()
    }
    
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            api("me.rahimklaber:stellar_kt:0.0.4")
            api("com.ionspin.kotlin:bignum:0.3.9")

            api(libs.arrow.core)
            api(libs.arrow.fx)
            api(libs.arrow.resilience)
        }
    }
}

