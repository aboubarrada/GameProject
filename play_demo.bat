@echo off
echo 🎮 Heart of the Void - Demo Jouable
echo ===================================

cd /d "%~dp0"

set JAVAFX_PATH=javafx\lib

echo 🔨 Compilation de la demo jouable...

if not exist "target\classes" mkdir "target\classes"

rem *** Correction : forcer UTF-8 ***
javac -encoding UTF-8 -d target\classes -cp "%JAVAFX_PATH%\*" src\demo\*.java

if %ERRORLEVEL% neq 0 (
    echo ❌ Erreur de compilation!
    echo 📋 Vérifiez que JavaFX est installé dans %JAVAFX_PATH%
    pause
    exit /b 1
)

echo ✅ Compilation réussie!
echo.
echo 🚀 Lancement de Heart of the Void avec Menu...
echo.
echo 🎯 CONTROLES:
echo    • Menu: Utilisez les boutons
echo    • Jeu: Cliquez pour placer des unités
echo    • Touches 1-4 pour sélectionner le type d'unité
echo    • ESPACE pour pause
echo    • R pour recommencer
echo.
echo 💜 Amusez-vous bien avec Heart of the Void!
echo.

java --module-path "%JAVAFX_PATH%" ^
     --add-modules javafx.controls ^
     -cp target\classes ^
     demo.SimpleBackgroundMenu

echo.
echo 👋 Merci d'avoir joué à Heart of the Void!
pause
