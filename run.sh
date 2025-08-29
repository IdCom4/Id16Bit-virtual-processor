#!/bin/bash
set -e  # stop if error

MAIN_CLASS="com.idcom4.Main"

# Dossiers
OUT_DIR="sh_out"
BUILD_DIR="sh_build"

# run
run() {
    java -cp "$OUT_DIR:$(cat $BUILD_DIR/cp.txt)" "$MAIN_CLASS" "$@"
}


run "$@"
