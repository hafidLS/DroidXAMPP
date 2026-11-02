# Status Pekerjaan DroidXAMPP

## Tahap 1 (fondasi build) — kode selesai, BELUM terbukti build
- Gradle wrapper 8.9 ditambahkan (gradlew, gradlew.bat, wrapper jar + properties).
- AGP 8.7.3, Kotlin 2.0.21, JDK 17, compileSdk/targetSdk 35.
- Dependensi dirapikan (navigation-compose dan ui-tooling-preview dihapus; coroutines ditambahkan eksplisit).
- Signing release dari secrets/env; tanpa debug signing untuk release. Lihat SIGNING.md.
- Manifest: ikon, hanya MainActivity yang diekspor.
- Workflow: wrapper-validation, lint, unit test, verifikasi artifact + apksigner, workflow_dispatch.
- Belum dijalankan: Gradle/AGP tidak dapat diunduh di lingkungan kerja ini, jadi build, lint, dan test belum dieksekusi.

## Belum dikerjakan
- Tahap 2 (integrasi Termux), Tahap 3 (script), UI, Database, File, Logs, Settings.
- Belum diuji di Redmi Note 13.
