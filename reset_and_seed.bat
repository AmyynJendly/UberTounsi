@echo off
set JAVA_HOME=C:\Program Files\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64
set MAVEN_HOME=C:\apache-maven-3.9.14
:: Standard MySQL paths - we search for the bin folder
set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.4\bin
if not exist "%MYSQL_BIN%" set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin

set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%MYSQL_BIN%;%PATH%

echo [1/3] Resetting Schema...
mysql -u root -p < db\schema.sql

echo [2/3] Compiling and Running SeedGenerator...
:: We include the main project classes in the classpath so SeedGenerator can use PasswordUtils
javac -cp "target\classes" SeedGenerator.java
java -cp ".;target\classes" SeedGenerator

echo [3/3] Importing Seed Data...
mysql -u root -p < db\seed.sql

echo Done! Now run start_server.bat
pause
