#!/data/data/com.termux/files/usr/bin/bash
. "$(dirname "$0")/common.sh"
need_cmd httpd
PIDFILE="${PREFIX}/var/run/apache2/httpd.pid"
running() { [ -f "$PIDFILE" ] && kill -0 "$(cat "$PIDFILE")" 2>/dev/null; }
case "$1" in
  start)
    if running; then echo "ALREADY_RUNNING"; exit 0; fi
    httpd -k start 2>&1; sleep 2
    if running && port_open "$APACHE_PORT"; then echo "STARTED"; log "apache start ok"
    else echo "ERROR: Apache gagal start (cek port $APACHE_PORT / error log)"; log "apache start gagal"; exit 1; fi ;;
  stop)
    if ! running; then echo "ALREADY_STOPPED"; exit 0; fi
    httpd -k stop 2>&1; sleep 1
    running && { echo "ERROR: masih berjalan"; exit 1; } || { echo "STOPPED"; log "apache stop"; } ;;
  restart) "$0" stop; "$0" start ;;
  status)
    if running; then echo "RUNNING pid=$(cat "$PIDFILE") port=$APACHE_PORT listening=$(port_open "$APACHE_PORT" && echo yes || echo no)"
    else echo "STOPPED"; exit 0; fi ;;
  version) httpd -v | head -1 ;;
  *) echo "usage: apache.sh {start|stop|restart|status|version}"; exit 2 ;;
esac
