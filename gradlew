#!/usr/bin/env bash
# This file is a shell script that invokes the Gradle wrapper.

# Use the same directory as this script
cd "$(dirname "$0")"

# Execute Gradle with the same arguments passed to this script
exec java -jar "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" "$@"