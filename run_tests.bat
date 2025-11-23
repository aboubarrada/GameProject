@echo off
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-25"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ================================
echo  Heart of the Void - Tests
echo ================================

if not exist "target\test-classes" mkdir "target\test-classes"

echo Compilation des classes de test...
javac -d target/test-classes -cp "target/classes;javafx/lib/*" test/demo/**/*.java
if %ERRORLEVEL% neq 0 (
    echo Erreur compilation tests
    pause
    exit /b 1
)

echo.
echo Execution des tests...
echo.

java -cp "target/classes;target/test-classes;javafx/lib/*" org.junit.platform.console.ConsoleLauncher --scan-classpath

echo.
echo Tests termines !
pause
