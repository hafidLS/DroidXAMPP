package com.droidxampp.termux

enum class Service(val script: String, val title: String) {
    APACHE("apache.sh", "Apache"),
    MARIADB("mariadb.sh", "MariaDB")
}
enum class Action(val arg: String) { START("start"), STOP("stop"), RESTART("restart"), STATUS("status"), VERSION("version") }

object TermuxCommand {
    const val TERMUX_PKG = "com.termux"
    const val HOME = "/data/data/com.termux/files/home"
    const val SCRIPT_DIR = "$HOME/droidxampp/scripts"
    const val BASH = "/data/data/com.termux/files/usr/bin/bash"
    const val RUN_ACTION = "com.termux.RUN_COMMAND"
    const val EXTRA_PATH = "com.termux.RUN_COMMAND_PATH"
    const val EXTRA_ARGS = "com.termux.RUN_COMMAND_ARGUMENTS"
    const val EXTRA_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
    const val EXTRA_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
    const val EXTRA_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT"

    fun scriptPath(service: Service) = "$SCRIPT_DIR/${service.script}"
}
