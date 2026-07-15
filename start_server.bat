@echo off
title CovoitDark Java Server
echo [INFO] Starting CovoitDark Backend...
echo [INFO] Environment: Native Java + Maven
echo.
set JAVA_HOME=C:\Program Files\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64
set MAVEN_HOME=C:\apache-maven-3.9.14
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo [INFO] Using Java: %JAVA_HOME%
echo [INFO] Using Maven: %MAVEN_HOME%


mvn clean compile exec:java -Dexec.mainClass="com.covoitdark.App"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Maven failed to start.
    echo [PROTIP] Ensure Maven and Java are in your PATH!
    pause
)
pause

    