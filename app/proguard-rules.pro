# Debug stack trace
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin metadata (讓 reflection 函式庫能運作)
-keep class kotlin.Metadata { *; }

# Coroutines 內部 reflection
-dontwarn kotlinx.coroutines.flow.**
