# Setup Termux untuk DroidXAMPP
1. Pasang Termux dari F-Droid/GitHub (bukan Play Store) dan DroidXAMPP dengan sumber yang sama-kepercayaan.
2. Di Termux, aktifkan izin integrasi:
   `mkdir -p ~/.termux && echo "allow-external-apps=true" >> ~/.termux/termux.properties` lalu `termux-reload-settings` (atau restart Termux).
   (Backup dulu: `cp ~/.termux/termux.properties ~/.termux/termux.properties.bak` bila file sudah ada.)
3. Di Android: Setelan > Aplikasi > DroidXAMPP > Izin > "Jalankan perintah di Termux" (com.termux.permission.RUN_COMMAND) = Izinkan.
4. Salin skrip: `mkdir -p ~/droidxampp/scripts && cp scripts/termux/*.sh ~/droidxampp/scripts/ && chmod +x ~/droidxampp/scripts/*.sh`
5. Uji manual: `~/droidxampp/scripts/apache.sh status` dan `~/droidxampp/scripts/mariadb.sh status`.
Catatan: Android dapat menghentikan Termux di latar belakang; gunakan `termux-wake-lock` dan matikan optimasi baterai untuk Termux. Hasil uji ketahanan akan dicatat di STATUS.md.
