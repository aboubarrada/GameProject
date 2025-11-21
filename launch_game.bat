@echo off
cd /d "%~dp0"

echo ================================
echo  Heart of the Void - Compilation
echo ================================

if not exist "target\classes" mkdir "target\classes"

echo Compilation des entites...
javac -d target\classes -cp "javafx\lib\*" src\demo\entities\*.java
if %ERRORLEVEL% neq 0 (
    echo Erreur compilation entities
    pause
    exit /b 1
)

echo Compilation des managers...
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\managers\*.java
if %ERRORLEVEL% neq 0 (
    echo Erreur compilation managers
    pause
    exit /b 1
)

echo Compilation de l'UI...
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\ui\*.java
if %ERRORLEVEL% neq 0 (
    echo Erreur compilation UI
    pause
    exit /b 1
)

echo Compilation de l'audio...
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\audio\*.java
if %ERRORLEVEL% neq 0 (
    echo Erreur compilation audio
    pause
    exit /b 1
)

echo Compilation du jeu principal...
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\HeartOfTheVoidGame.java
if %ERRORLEVEL% neq 0 (
    echo Erreur compilation principale
    pause
    exit /b 1
)

echo Compilation des menus...
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\menus\*.java
if %ERRORLEVEL% neq 0 (
    echo Erreur compilation menus
    pause
    exit /b 1
)

echo Compilation du menu simple...
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\SimpleBackgroundMenu.java
if %ERRORLEVEL% neq 0 (
    echo Erreur compilation menu simple
    pause
    exit /b 1
)

echo.
echo ================================
echo  Lancement du jeu...
echo ================================
echo.

REM Tentative avec module-path
java --module-path "javafx\lib" --add-modules javafx.controls,javafx.media -cp target\classes demo.menus.LevelSelectMenu

REM Si échec, tentative avec classpath simple
if %ERRORLEVEL% neq 0 (
    echo.
    echo Tentative avec classpath simple...
    java -Djava.library.path="javafx\lib" -cp "javafx\lib\*;target\classes" demo.menus.LevelSelectMenu
)

REM Si échec, lancement du menu simple
if %ERRORLEVEL% neq 0 (
    echo.
    echo Lancement du menu de base...
    java -Djava.library.path="javafx\lib" -cp "javafx\lib\*;target\classes" demo.SimpleBackgroundMenu
)

echo.
pause