# R8 上線前設定

## 步驟

### 1. 啟用 minify + shrink

在 `app/build.gradle.kts` 把 `release` 的 `isMinifyEnabled` 和 `isShrinkResources` 改回 `true`：

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        signingConfig = signingConfigs.getByName("release")
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### 2. `proguard-rules.pro` 內容

```proguard
# Debug stack trace
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin metadata (讓 reflection 函式庫能運作)
-keep class kotlin.Metadata { *; }

# Coroutines 內部 reflection
-dontwarn kotlinx.coroutines.flow.**

# App 自身 code (Koin DI lambda 建立 UseCase/ViewModel/Repository)
-keep class com.example.myapplication.** { *; }
```

### 3. Clean build

```powershell
./gradlew clean assembleRelease
```

### 4. 驗證

- 安裝 APK 後完整跑一遍所有功能
- 檢查 `app/build/outputs/mapping/release/usage.txt` 有無誤砍 class
- 如果 crash → 加精準 `-keep class xxx { *; }`（不是 `**`）

### 5. 上架前保留

保留 `app/build/outputs/mapping/release/mapping.txt` 用於反混淆 crash stack trace。
