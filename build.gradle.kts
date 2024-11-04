// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
    alias(libs.plugins.kotlinCompose) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}