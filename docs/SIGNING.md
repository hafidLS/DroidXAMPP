# Signing APK Release

Keystore dan password TIDAK ada di source code. Buat sendiri sekali, lalu simpan di GitHub Secrets.
Simpan cadangan keystore di tempat aman: jika hilang, update APK tidak bisa dipasang di atas versi lama.

## 1. Buat keystore (di Termux, pkg install openjdk-17)
    keytool -genkeypair -v -keystore droidxampp-release.jks -alias droidxampp \
      -keyalg RSA -keysize 2048 -validity 10000

Isi password dengan sendiri (jangan dibagikan).

## 2. Encode ke base64
    base64 -w0 droidxampp-release.jks > keystore.b64

## 3. Isi GitHub Secrets (Repo > Settings > Secrets and variables > Actions)
- ANDROID_KEYSTORE_BASE64  = isi file keystore.b64
- ANDROID_KEYSTORE_PASSWORD
- ANDROID_KEY_ALIAS        = droidxampp
- ANDROID_KEY_PASSWORD

Hapus keystore.b64 dari penyimpanan umum setelah selesai. File *.jks dan *.b64 sudah ada di .gitignore.

## Perilaku workflow
- 4 secret lengkap: APK release bertanda tangan, diverifikasi dengan apksigner.
- Semua kosong: dibangun APK debug (hanya uji coba), dengan peringatan.
- Sebagian terisi: workflow gagal dengan pesan jelas.
