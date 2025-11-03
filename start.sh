#!/bin/bash

echo "💜 Heart of the Void - Lancement du jeu"
echo

# Fonction pour ouvrir le navigateur selon l'OS
open_browser() {
    local url=$1
    if command -v xdg-open > /dev/null; then
        xdg-open "$url"
    elif command -v open > /dev/null; then
        open "$url"
    else
        echo "Ouvrez manuellement: $url"
    fi
}

# Vérifier si Python est disponible
if command -v python3 > /dev/null; then
    echo "Python 3 détecté - Démarrage du serveur HTTP..."
    open_browser "http://localhost:8000"
    python3 -m http.server 8000
elif command -v python > /dev/null; then
    echo "Python détecté - Démarrage du serveur HTTP..."
    open_browser "http://localhost:8000"
    python -m http.server 8000
# Vérifier si Node.js est disponible
elif command -v node > /dev/null; then
    echo "Node.js détecté - Installation et démarrage du serveur..."
    npm install -g http-server 2>/dev/null
    open_browser "http://localhost:8080"
    npx http-server -p 8080
else
    echo
    echo "⚠️  Aucun serveur détecté !"
    echo
    echo "Pour jouer, vous pouvez :"
    echo "1. Ouvrir index.html directement dans votre navigateur"
    echo "2. Installer Python ou Node.js pour un serveur local"
    echo
    echo "Tentative d'ouverture du fichier index.html..."
    open_browser "file://$(pwd)/index.html"
fi