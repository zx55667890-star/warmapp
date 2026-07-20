# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# === 預設 optimize 配置已處理大部分情況 ===
# (proguard-android-optimize.txt 已經包含 Android 框架的 keep 規則)

# === Kotlin / Coroutines ===
-keep class kotlin.Metadata { *; }
-dontwarn kotlinx.coroutines.flow.**

# === Debug / 除錯用 ===
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
