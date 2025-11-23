# Heart of the Void

Un jeu de stratégie en temps réel (Tower Defense) développé en Java avec JavaFX.

## Description

Heart of the Void est un jeu où vous devez défendre votre base contre des vagues d'ennemis en déployant des unités stratégiquement. Le jeu propose 3 niveaux de difficulté progressive avec des mécaniques de gameplay évolutives.

## Caractéristiques

- **3 niveaux jouables** : Radiance Arena (facile), City of Tears (moyen), Nightmare Realm (difficile)
- **4 types d'unités** avec des capacités différentes
- **Système de progression** : difficulté augmentant avec le temps
- **Régénération passive d'argent**
- **Interface graphique complète** avec menus et système de sélection
- **Musique et effets sonores** pour chaque niveau

## Prérequis

- Java JDK 11 ou supérieur
- JavaFX (inclus dans le dossier `javafx/lib`)

## Installation

1. Cloner le dépôt :

```bash
git clone https://github.com/awxzed/GameProject.git
cd GameProject-2
```

2. Vérifier que le JDK est installé :

```bash
java -version
```

## Lancement du jeu

### Windows

Double-cliquez sur `launch_game.bat` ou exécutez dans un terminal :

```bash
launch_game.bat
```

## Commandes

- **1-4** : Sélectionner un type d'unité
- **Clic** : Placer l'unité sélectionnée
- **Espace** : Pause
- **R** : Redémarrer le niveau
- **Échap** : Retour au menu

## Types d'unités

| Unité   | Coût | HP  | Dégâts | Range | Attaque/s |
| ------- | ---- | --- | ------ | ----- | --------- |
| Knight  | 40   | 80  | 25     | 100   | 1.5       |
| Vessel  | 60   | 60  | 30     | 120   | 2.0       |
| Hornet  | 85   | 50  | 35     | 150   | 2.5       |
| GodVoid | 150  | 120 | 50     | 140   | 3.0       |

## Structure du projet

```
GameProject-2/
├── src/demo/           # Code source principal
│   ├── entities/       # Classes des entités (unités, bases, projectiles)
│   ├── managers/       # Logique de jeu
│   ├── menus/          # Interfaces des menus
│   ├── ui/             # Rendu de l'interface
│   └── audio/          # Gestion audio
├── test/               # Tests unitaires
├── resources/          # Images et sons
├── javafx/             # Bibliothèques JavaFX
└── target/classes/     # Fichiers compilés
```

## Tests

Des tests unitaires sont disponibles dans le dossier `test/`. Pour les exécuter :

```bash
javac -d target/classes -cp "javafx/lib/*;junit.jar" test/demo/**/*.java
java -cp "javafx/lib/*;junit.jar;target/classes" org.junit.runner.JUnitCore demo.entities.GameUnitTest
```

## Auteurs

- Aymen
