@echo off
@if "%OS%"=="Windows_NT" (
   setlocal enabledelayedexpansion
   set JAR_PATH=%~dp0gradle\wrapper\gradle-wrapper.jar
   java -jar "!JAR_PATH!" %*
) else (
   exec java -jar "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" "$@"
)