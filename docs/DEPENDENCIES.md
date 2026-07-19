# DEPENDENCIES.md — 版本依賴資訊

## SDK 版本
| 項目 | 值 |
|------|-----|
| compileSdk | 37 |
| targetSdk | 37 |
| minSdk | 28 |
| namespace | com.example.myapplication |
| AGP | 9.3.0 |
| Kotlin | 2.2.20 |
| Gradle | 9.5.0 |

## AndroidX / Jetpack Compose
| 函式庫 | 版本 | 備註 |
|--------|------|------|
| Compose BOM | 2026.05.01 | Material3 |
| core-ktx | 1.19.0 | |
| activity-compose | 1.13.0 | |
| lifecycle-runtime-ktx | 2.10.0 | 需與 lifecycle 一致 |
| lifecycle-runtime-compose | 2.10.0 | 閃退風險 if 版本不一致 |
| lifecycle-viewmodel-compose | 2.10.0 | |
| lifecycle-viewmodel-ktx | 2.10.0 | |
| navigation-compose | 2.8.0 | |
| material | 1.12.0 | Material (non-Compose) |
| exifinterface | 1.3.7 | |

## Firebase
| 函式庫 | 版本 |
|--------|------|
| firebase-database | 22.0.1 |
| firebase-auth | 23.2.0 |
| firebase-messaging | 24.1.0 |
| firebase-storage | 21.0.1 |
| firebase-functions | 21.1.0 |

## 媒體 / 相機
| 函式庫 | 版本 |
|--------|------|
| CameraX | 1.5.0（core/camera2/lifecycle/view/video） |
| Media3 (ExoPlayer) | 1.6.0（exoplayer/ui/database） |
| Coil | 2.7.0（compose/video） |

## DI / Async
| 函式庫 | 版本 |
|--------|------|
| Koin | 4.1.0（core/android/compose） |
| kotlinx-coroutines-play-services | 1.9.0 |
| kotlinx-coroutines-test | 1.9.0 |

## Google 服務
| 函式庫 | 版本 |
|--------|------|
| play-services-auth | 21.2.0 |
| google-genai | 1.61.0 |

## 測試
| 函式庫 | 版本 |
|--------|------|
| JUnit | 4.13.2 |
| androidx-junit | 1.3.0 |
| espresso-core | 3.7.0 |
| mockk | 1.13.5 |

## 後端 (functions/package.json)
| 套件 | 版本 |
|------|------|
| Node.js | 22 (runtime) |
| firebase-admin | ^13.0.0 |
| firebase-functions | 7.2.5 |
| @google/genai | ^2.10.0 |
| nodemailer | ^6.9.0 |

## Cloud Function 自我修復
| 項目 | 值 |
|------|-----|
| 機制 | `healOrphanedPending()` 掃描 `solutions/{uid}` 中 PENDING > 10 分鐘的孤立 entry |
| 批次 | 每次 5 個 user |
| 游標 | `config/repair_cursor` 記錄進度 |
| 原因 | `saveSkill()` 使用兩次個別 `setValue()` 非原子寫入，`pending_skills` 可能寫失敗 |

## Cloud Function 模型清單 (fallback 順序)
| 順位 | 模型 | 思考 | 搜尋 |
|------|------|------|------|
| PRIMARY | gemini-3.1-flash-lite | - | ❌ | 最高 RPD 主力，快速濾掉已知技能 |
| FALLBACK_1 | gemini-3.1-flash-lite | - | Serper | 同 PRIMARY + Serper 外部搜尋 |
| FALLBACK_2 | gemini-2.5-flash-lite | budget:0 | ✅ googleSearch | 內建 Google Search |
| FALLBACK_3 | gemini-2.5-flash | budget:0 | ✅ googleSearch | 內建 Google Search |
| FALLBACK_4 | gemini-3.5-flash | minimal | Serper | Gen3 + Serper |
| FALLBACK_5 | gemini-3-flash-preview | minimal | Serper | Gen3 + Serper |
