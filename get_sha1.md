在模擬器那台執行以下指令取得 debug SHA-1 fingerprint，然後加到 Firebase Console：

```powershell
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

輸出中找 `SHA1:` 那行，整串複製到 Firebase Console → Project Settings → 你的 Android app → SHA certificate fingerprints → 新增。
