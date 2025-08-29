#!/bin/bash
set -e  # stop if error

# Dossiers
OUT_DIR="sh_out"
BUILD_DIR="sh_build"

# full clean
clean() {
    echo "🧹 Cleaning ..."
    rm -rf "$OUT_DIR" "$BUILD_DIR"
    echo "🔥 Done !"
}

# Build
buildProject() {
    mkdir -p "$OUT_DIR" "$BUILD_DIR"

    echo "📦 Fetching the Maven dependencies ..."
    mvn dependency:build-classpath -Dmdep.outputFile="$BUILD_DIR/cp.txt" > /dev/null

    echo "📄 Fetching sources ..."
    find src/main/java -name "*.java" > "$BUILD_DIR/sources.txt"

    echo "🔨 Compiling ..."
    javac -cp "$OUT_DIR:$(cat $BUILD_DIR/cp.txt)" -d "$OUT_DIR" @"$BUILD_DIR/sources.txt"

    echo "🔥 Done !"
}

if [ "$#" -eq 0 ]
then
      buildProject
else
  case "$1" in
      clean)
          clean
          ;;
      *)
          echo "Usage: $0 {clean}"
          echo "  clean  -> deletes every build artifacts"
          exit 1
          ;;
  esac
fi
