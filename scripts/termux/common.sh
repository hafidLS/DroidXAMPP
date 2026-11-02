#!/data/data/com.termux/files/usr/bin/bash
# Fungsi bersama untuk skrip DroidXAMPP. Tidak berisi password.
DX_HOME="${DX_HOME:-$HOME/droidxampp}"
DX_CONF="$DX_HOME/droidxampp.conf"
[ -f "$DX_CONF" ] && . "$DX_CONF"
APACHE_PORT="${APACHE_PORT:-8080}"
HTDOCS="${HTDOCS:-$HOME/htdocs}"
LOG_DIR="$DX_HOME/logs"
mkdir -p "$LOG_DIR"

port_open() {  # port_open <port> -> exit 0 jika ada yang listen
  (exec 3<>/dev/tcp/127.0.0.1/"$1") 2>/dev/null && { exec 3>&- 3<&-; return 0; } || return 1
}
need_cmd() { command -v "$1" >/dev/null 2>&1 || { echo "ERROR: perintah '$1' tidak ditemukan"; exit 3; }; }
log() { echo "$(date '+%F %T') $*" >> "$LOG_DIR/app.log"; }
