@echo off
cd /d "%~dp0"

echo Nettoyage...
if exist "target\classes" rmdir /s /q "target\classes"
mkdir "target\classes"

echo Compilation...
javac -d target\classes -cp "javafx\lib\*" src\demo\entities\*.java
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\managers\*.java
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\ui\*.java
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\audio\*.java
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\menus\*.java
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\HeartOfTheVoidGame.java
javac -d target\classes -cp "javafx\lib\*;target\classes" src\demo\SimpleBackgroundMenu.java

echo Lancement...
java -cp "javafx\lib\*;target\classes" demo.SimpleBackgroundMenu

pause