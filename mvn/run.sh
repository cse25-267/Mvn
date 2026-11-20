#!/bin/bash
# Compiles and starts the Banking System with Maven. Run with:  bash run.sh
set -e
cd "$(dirname "$0")"

export DISPLAY="${DISPLAY:-:1}"

# Wait for MySQL to be ready (it can take ~30 seconds the very first time)
HOST="${DB_HOST:-localhost}"
echo "Waiting for the database at $HOST:3306..."
for i in $(seq 1 60); do
    if (echo > /dev/tcp/$HOST/3306) 2>/dev/null; then break; fi
    sleep 1
done

echo "Starting... open the 'Desktop' port (6080) in your browser to see the window."
mvn -q compile javafx:run
