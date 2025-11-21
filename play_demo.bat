@echo off
cd /d "%~dp0"

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

echo Compilation des menus...
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\menus\*.java
if %ERRORLEVEL% neq 0 (
    echo Erreur compilation menus
    pause
    exit /b 1
)

echo Compilation du jeu principal...
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\*.java
if %ERRORLEVEL% neq 0 (
    echo Erreur compilation principale
    pause
    exit /b 1
)

echo Lancement du menu de selection...
java -cp "javafx\lib\*;target\classes" demo.menus.LevelSelectMenu

pause