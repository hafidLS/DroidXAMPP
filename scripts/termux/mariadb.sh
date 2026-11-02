#!/data/data/com.termux/files/usr/bin/bash
. "$(dirname "$0")/common.sh"
need_cmd mariadbd-safe; need_cmd mariadb-admin
MARIA_PORT="${MARIA_PORT:-3306}"
alive() { mariadb-admin ping --silent 2>/dev/null; }   # memakai socket lokal, tanpa password
case "$1" in
  start)
    if alive; then echo "ALREADY_RUNNING"; exit 0; fi
    # bind-address=127.0.0.1 agar hanya menerima koneksi lokal
    nohup mariadbd-safe --bind-address=127.0.0.1 --port="$MARIA_PORT" >"$LOG_DIR/mariadb-start.log" 2>&1 &
    for i in 1 2 3 4 5 6 7 8 9 10; do sleep 1; alive && break; done
    alive && { echo "STARTED"; log "mariadb start ok"; } || { echo "ERROR: MariaDB gagal start (lihat $LOG_DIR/mariadb-start.log)"; exit 1; } ;;
  stop)
    if ! alive; then echo "ALREADY_STOPPED"; exit 0; fi
    mariadb-admin shutdown 2>&1; sleep 2
    alive && { echo "ERROR: masih berjalan"; exit 1; } || { echo "STOPPED"; log "mariadb stop"; } ;;
  restart) "$0" stop; "$0" start ;;
  status) alive && echo "RUNNING port=$MARIA_PORT" || echo "STOPPED" ;;
  version) mariadbd --version 2>/dev/null | head -1 ;;
  *) echo "usage: mariadb.sh {start|stop|restart|status|version}"; exit 2 ;;
esac
