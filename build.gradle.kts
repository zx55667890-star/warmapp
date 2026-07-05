plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

// 統一將所有編譯產物導向你指定的本地路徑
allprojects {
    layout.buildDirectory.set(file("C:/Build Cache/warmapp/${rootProject.name}/${project.name}"))
}

val defaultApplicationId by extra("com.example.myapplication")