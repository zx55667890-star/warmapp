在模擬器那台執行以下指令取得 debug SHA-1 fingerprint，然後加到 Firebase Console：

### 方法一：Android Studio 內建 JDK
```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

### 方法二：系統 Java（如果有裝）
```powershell
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

輸出中找 `SHA1:` 那行，整串複製到 **Firebase Console → Project Settings → 你的 Android app → SHA certificate fingerprints → 新增**。

