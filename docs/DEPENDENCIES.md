# DEPENDENCIES.md — 版本依賴資訊

更新日：所有日期均為 2026/07/20
第 1 波：lifecycle / coroutines / play-services-auth / mockk / navigation-compose / CameraX
第 2 波：Kotlin / Gradle / Compose BOM
第 3 波：Media3 / Coil / Koin / 後端 npm
第 4 波：Firebase Auth/Messaging/Storage/Functions
第 5 波：exifinterface / Media3 / coroutines / play-services-auth / genai / mockk / Firebase BoM / Node.js 24
第 6 波：material-icons-extended → core（省 ~2.5min dexing），JVM target 17，gradle.properties 調校
注意：`material = 1.12.0`（View 系統）已於 7/20 移除，改用 `@android:style/Theme.Material.Light.NoActionBar`。App 已是 100% Compose。 

## SDK 版本
| 項目 | 值 | 更新日 |
|------|-----|--------|
| compileSdk | 37 | 沒更新 |
| targetSdk | 37 | 沒更新 |
| minSdk | 28 | 沒更新 |
| namespace | com.example.myapplication | 沒更新 |
| AGP | 9.3.0 | 沒更新 |
| Kotlin | 2.4.10 | 7/20 |
| Gradle | 9.6.1 | 7/20 |

## AndroidX / Jetpack Compose
| 函式庫 | 版本 | 更新日 | 備註 |
|--------|------|--------|------|
| Compose BOM | 2026.06.00 | 7/20 | Material3 |
| Material Icons | core（原 extended） | 7/20 | 改用 core 省 ~2.5min dexing |
| core-ktx | 1.19.0 | 沒更新 | |
| activity-compose | 1.13.0 | 沒更新 | |
| lifecycle-runtime-ktx | 2.11.0 | 7/20 | |
| lifecycle-runtime-compose | 2.11.0 | 7/20 | |
| lifecycle-viewmodel-compose | 2.11.0 | 7/20 | |
| lifecycle-viewmodel-ktx | 2.11.0 | 7/20 | |
| navigation-compose | 2.9.8 | 7/20 | |
| exifinterface | 1.4.2 | 7/20 | |

## Firebase
| 函式庫 | 版本 | 更新日 | 備註 |
|--------|------|--------|------|
| firebase-bom | 34.16.0 | 7/20 | 統一管理所有 Firebase SDK 版本 |
| firebase-database | (由 BoM 管理) | 7/20 | |
| firebase-auth | (由 BoM 管理) | 7/20 | |
| firebase-messaging | (由 BoM 管理) | 7/20 | |
| firebase-storage | (由 BoM 管理) | 7/20 | |
| firebase-functions | (由 BoM 管理) | 7/20 | |

## 媒體 / 相機
| 函式庫 | 版本 | 更新日 |
|--------|------|--------|
| CameraX | 1.6.1（core/camera2/lifecycle/view/video） | 7/20 |
| Media3 (ExoPlayer) | 1.10.1（exoplayer/ui/database） | 7/20 |
| Coil | 3.5.0（compose/video/network-okhttp） | 7/20 |

## DI / Async
| 函式庫 | 版本 | 更新日 |
|--------|------|--------|
| Koin | 4.2.2（core/android/compose） | 7/20 |
| kotlinx-coroutines-play-services | 1.11.0 | 7/20 |
| kotlinx-coroutines-test | 1.11.0 | 7/20 |

## Google 服務
| 函式庫 | 版本 | 更新日 |
|--------|------|--------|
| play-services-auth | 21.6.0 | 7/20 |
| google-genai | 1.62.0 | 7/20 |

## 測試
| 函式庫 | 版本 | 更新日 |
|--------|------|--------|
| JUnit | 4.13.2 | 沒更新 |
| androidx-junit | 1.3.0 | 沒更新 |
| espresso-core | 3.7.0 | 沒更新 |
| mockk | 1.14.11 | 7/20 |

## 後端 (functions/package.json)
| 套件 | 版本 | 更新日 |
|------|------|--------|
| Node.js | 24 (runtime) | 7/20 |
| firebase-admin | ^14.2.0 | 7/20 |
| firebase-functions | 7.3.0 | 7/20 |
| @google/genai | ^2.12.0 | 7/20 |
| nodemailer | ^9.0.3 | 7/20 |

## Cloud Function 自我修復
| 項目 | 值 |
|------|-----|
| 機制 | `healOrphanedPending()` 掃描 `solutions/{uid}` 中 PENDING > 10 分鐘的孤立 entry |
| 批次 | 每次 5 個 user |
| 游標 | `config/repair_cursor` 記錄進度 |
| 原因 | `saveSkill()` 使用兩次個別 `setValue()` 非原子寫入，`pending_skills` 可能寫失敗 |

## Cloud Function 模型清單 (fallback 順序)
| 順位 | 模型 | 思考 | 搜尋 | 備註 |
|------|------|------|------|------|
| PRIMARY | gemini-3.1-flash-lite | - | ❌ | 最高 RPD 主力，快速濾掉已知技能 |
| FALLBACK_1 | gemini-3.1-flash-lite | - | Serper | 同 PRIMARY + Serper 外部搜尋 |
| FALLBACK_2 | gemini-2.5-flash-lite | budget:0 | ✅ googleSearch | 內建 Google Search |
| FALLBACK_3 | gemini-2.5-flash | budget:0 | ✅ googleSearch | 內建 Google Search |
| FALLBACK_4 | gemini-3.5-flash | minimal | Serper | Gen3 + Serper |
| FALLBACK_5 | gemini-3-flash-preview | minimal | Serper | Gen3 + Serper |
