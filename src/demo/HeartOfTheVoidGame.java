package demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import demo.entities.GameUnit;
import demo.entities.GameBase;
import demo.entities.GameProjectile;
import demo.managers.GameManager;
import demo.ui.UIRenderer;
import demo.audio.AudioManager;

class BasePosition {
    double playerX, enemyX, floorY;
    
    BasePosition(double playerX, double enemyX, double floorY) {
        this.playerX = playerX;
        this.enemyX = enemyX;
        this.floorY = floorY;
    }
}

public class HeartOfTheVoidGame {
    
    private static final int CANVAS_WIDTH = 1000;
    private static final int CANVAS_HEIGHT = 600;
    private static final int INITIAL_ENERGY = 80;
    private static final int MAX_ENERGY = 150;
    
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean gameEnded = false;
    private String gameEndMessage = "";
    private Color gameEndColor = Color.WHITE;
    
    private int energy = INITIAL_ENERGY;
    private int wave = 1;
    private int score = 0;
    private int selectedUnitType = 1;
    private double lastEnemySpawn = 0;
    private double gameTime = 0;
    private int enemiesKilledThisWave = 0;
    private int enemiesNeededForNextWave = 10;
    private double difficultyMultiplier = 1.0;
    
    private List<GameUnit> allies = new ArrayList<>();
    private List<GameUnit> enemies = new ArrayList<>();
    private List<GameProjectile> projectiles = new ArrayList<>();
    
    private GameBase playerBase;
    private GameBase enemyBase;
    
    private GameManager gameManager;
    private UIRenderer uiRenderer;
    private AudioManager audioManager;
    
    private int currentLevel = 1;
    
    private Image[] allyImages = new Image[4];
    private Image[] enemyImages = new Image[4];
    private Image[] projectileImages = new Image[3];
    
    private Stage gameStage;
    
    public void start(Stage stage) {
        this.gameStage = stage;
        gameManager = new GameManager();
        uiRenderer = new UIRenderer();
        audioManager = new AudioManager();
        setupUI(stage);
        initializeGame();
        audioManager.playBackgroundMusic(currentLevel);
        startGameLoop();
    }
    
    public void setLevel(int level) {
        this.currentLevel = level;
    }
    
    private void setupUI(Stage stage) {
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        try {
            allyImages[0] = new Image("file:resources/images/allies/The_Knight_Idle.png");
            allyImages[1] = new Image("file:resources/images/allies/200px-Lord_of_Shades.png");
            allyImages[2] = new Image("file:resources/images/allies/Hornet_Idle.png");
            allyImages[3] = new Image("file:resources/images/allies/200px-God_of_Gods.png");
            
            enemyImages[0] = new Image("file:resources/images/enemies/113px-B_Violent_Husk.png");
            enemyImages[1] = new Image("file:resources/images/enemies/120px-Broken_Vessel_Idle.png");
            enemyImages[2] = new Image("file:resources/images/enemies/120px-B_Furious_Vengefly.png");
            enemyImages[3] = new Image("file:resources/images/enemies/113px-B_Radiance.png");
            
            projectileImages[0] = new Image("file:resources/images/projectiles/Void_projectile.png");
            projectileImages[1] = new Image("file:resources/images/projectiles/Hornet_projectile.png");
            projectileImages[2] = new Image("file:resources/images/projectiles/ennemy_projectile.png");
            
            System.out.println("Toutes les images chargées avec succès!");
        } catch (Exception e) {
            System.out.println("Erreur chargement images: " + e.getMessage());
        }
        
    canvas.setOnMouseClicked(this::handleMouseClick);
        
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DIGIT1) selectedUnitType = 1;
            else if (e.getCode() == KeyCode.DIGIT2) selectedUnitType = 2;
            else if (e.getCode() == KeyCode.DIGIT3) selectedUnitType = 3;
            else if (e.getCode() == KeyCode.DIGIT4) selectedUnitType = 4;
            else if (e.getCode() == KeyCode.SPACE) togglePause();
            else if (e.getCode() == KeyCode.R) restartGame();
            else if (e.getCode() == KeyCode.ESCAPE) returnToMenu();
        });
        
        scene.getRoot().setFocusTraversable(true);
        scene.getRoot().requestFocus();
        
        stage.setTitle("Heart of the Void - Demo Jouable");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            audioManager.stopCurrentMusic();
            stopGame();
        });
    }
    
    private void initializeGame() {
        // Calculer les positions des bases selon le niveau
        BasePosition positions = getBasePositionsForLevel(currentLevel);
        
        playerBase = new GameBase(positions.playerX, positions.floorY, true);
        enemyBase = new GameBase(positions.enemyX, positions.floorY, false);
        
        // Debug pour vérifier les coordonnées
        System.out.println("Bases créées pour le niveau " + currentLevel + ":");
        System.out.println("   PlayerBase: x=" + playerBase.x + ", y=" + playerBase.y);
        System.out.println("   EnemyBase: x=" + enemyBase.x + ", y=" + enemyBase.y);
        System.out.println("   FloorY calculé: " + positions.floorY);
        
        allies.clear();
        enemies.clear();
        projectiles.clear();
        
        energy = INITIAL_ENERGY;
        wave = 1;
        score = 0;
        gameTime = 0;
        lastEnemySpawn = 0;
        enemiesKilledThisWave = 0;
        enemiesNeededForNextWave = 10;
        difficultyMultiplier = 1.0;
        isRunning = true;
        isPaused = false;
        
        // Reset des variables de fin de jeu
        gameEnded = false;
        gameEndMessage = "";
        gameEndColor = Color.WHITE;
    }
    
    private void handleMouseClick(MouseEvent event) {
        if (!isRunning || isPaused) return;

        double x = event.getX();
        double y = event.getY();

        int[] unitCosts = new int[] {40,60,85,150};
        int cardCount = unitCosts.length;
        int cardW = 90;
        int cardH = 90;
        int spacing = 16;
        int totalW = cardCount * cardW + (cardCount - 1) * spacing;
        int startX = (CANVAS_WIDTH - totalW) / 2;
        int yTop = 10;

        if (y >= yTop && y <= yTop + cardH) {
            for (int i = 0; i < cardCount; i++) {
                int cx = startX + i * (cardW + spacing);
                if (x >= cx && x <= cx + cardW) {
                    selectedUnitType = i + 1;
                    return;
                }
            }
        }
        BasePosition positions = getBasePositionsForLevel(currentLevel);
        double floorY = positions.floorY;
        int floorTolerance = 40;

        if (x > 150 && x < CANVAS_WIDTH * 0.6 && 
            y > floorY - floorTolerance && y < floorY + floorTolerance) {
            placeAlly(x, floorY);
        }
    }
    
    private void placeAlly(double x, double y) {
        int cost = gameManager.getAllyCost(selectedUnitType);
        if (energy >= cost) {
            GameUnit ally = gameManager.createAlly(selectedUnitType, x, y);
            if (ally != null) {
                allies.add(ally);
                energy -= cost;
            }
        }
    }
    
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                
                double deltaTime = (now - lastUpdate) / 1e9;
                lastUpdate = now;
                
                if (!isPaused && isRunning && !gameEnded) {
                    updateGame(deltaTime);
                }
                renderGame();
            }
        };
        gameLoop.start();
    }
    
    private void updateGame(double deltaTime) {
        gameTime += deltaTime;
        
        energy = Math.min(MAX_ENERGY, energy + (int)(3 * deltaTime));
        
        double spawnInterval = gameManager.calculateSpawnInterval(wave, difficultyMultiplier);
        
        if (gameTime - lastEnemySpawn > spawnInterval) {
            spawnEnemy();
            lastEnemySpawn = gameTime;
        }
        
        updateUnits(deltaTime);
        updateProjectiles(deltaTime);
        updateBases(deltaTime);
        
        checkGameEnd();
    }
    
    private void spawnEnemy() {
        BasePosition positions = getBasePositionsForLevel(currentLevel);
        GameUnit enemy = gameManager.createEnemy(wave, difficultyMultiplier, positions.floorY);
        enemies.add(enemy);
    }
    
    private void progressToNextWave() {
        wave++;
        enemiesKilledThisWave = 0;
        enemiesNeededForNextWave = 10 + wave * 3;
        difficultyMultiplier += 0.25;
        
        energy = Math.min(MAX_ENERGY, energy + 15);
        System.out.println("Vague " + wave + " ! Difficulté: " + String.format("%.1f", difficultyMultiplier));
    }
    
    private void updateUnits(double deltaTime) {
        updateUnitList(allies, enemies, deltaTime, true);
        updateUnitList(enemies, allies, deltaTime, false);
    }
    
    private void updateUnitList(List<GameUnit> units, List<GameUnit> targets, double deltaTime, boolean isAlly) {
        Iterator<GameUnit> it = units.iterator();
        while (it.hasNext()) {
            GameUnit unit = it.next();
            
            if (!unit.isAlive()) {
                if (!isAlly) {
                    int reward = gameManager.getEnemyReward(unit.unitType, wave);
                    energy += reward;
                    score += reward * 2;
                    enemiesKilledThisWave++;
                    
                    if (enemiesKilledThisWave >= enemiesNeededForNextWave) {
                        progressToNextWave();
                    }
                }
                it.remove();
                continue;
            }
            
            unit.update(deltaTime);
            GameUnit target = gameManager.findClosestTarget(unit, targets);
            if (target == null) {
                GameBase targetBase = isAlly ? enemyBase : playerBase;
                unit.moveTowards(targetBase.x, targetBase.y, deltaTime);
                
                if (unit.distanceTo(targetBase.x, targetBase.y) < unit.range) {
                    if (unit.canAttack()) {
                        targetBase.takeDamage(unit.damage);
                        unit.resetAttackCooldown();
                    }
                }
            } else {
                if (unit.distanceTo(target.x, target.y) <= unit.range) {
                    if (unit.canAttack()) {
                        GameProjectile proj = gameManager.createProjectile(unit, target, isAlly);
                        projectiles.add(proj);
                        unit.resetAttackCooldown();
                    }
                } else {
                    unit.moveTowards(target.x, target.y, deltaTime);
                }
            }
        }
    }
    
    private void updateProjectiles(double deltaTime) {
        Iterator<GameProjectile> it = projectiles.iterator();
        while (it.hasNext()) {
            GameProjectile proj = it.next();
            proj.update(deltaTime);
            
            if (proj.isExpired()) {
                it.remove();
                continue;
            }
            
            boolean hit = false;
            
            if (proj.color == Color.CYAN) {
                for (GameUnit enemy : enemies) {
                    if (proj.collidesWith(enemy)) {
                        enemy.takeDamage(proj.damage);
                        hit = true;
                        break;
                    }
                }
            } else if (proj.color == Color.RED) {
                for (GameUnit ally : allies) {
                    if (proj.collidesWith(ally)) {
                        ally.takeDamage(proj.damage);
                        hit = true;
                        break;
                    }
                }
            }
            
            if (hit) {
                it.remove();
            }
        }
    }
    
    private void updateBases(double deltaTime) {
        playerBase.update(deltaTime);
        enemyBase.update(deltaTime);
    }
    
    private void checkGameEnd() {
        if (!playerBase.isAlive()) {
            if (!gameEnded) {
                isRunning = false;
                gameEnded = true;
                gameEndMessage = "DÉFAITE";
                gameEndColor = Color.RED;
                
                allies.clear();
                enemies.clear();
                projectiles.clear();
                
                if (gameLoop != null) {
                    gameLoop.stop();
                }
                
                System.out.println("Partie terminée - DÉFAITE");
            }
            
        } else if (!enemyBase.isAlive() && isRunning) {
            wave++;
            
            gameEndMessage = "VICTOIRE - Vague " + wave;
            gameEndColor = Color.GOLD;
            
            enemies.clear();
            projectiles.clear();
            
            BasePosition positions = getBasePositionsForLevel(currentLevel);
            enemyBase = new GameBase(positions.enemyX, positions.floorY, false);
            enemyBase.health = enemyBase.maxHealth = 300 + wave * 100;
            energy = Math.min(MAX_ENERGY, energy + 25);
            score += 100;
            
            enemiesKilledThisWave = 0;
            enemiesNeededForNextWave = 10 + wave * 3;
            difficultyMultiplier += 0.3;
            
            System.out.println("Victoire ! Vague " + wave + " commence");
            
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> {
                    gameEndMessage = "";
                })
            );
            timeline.play();
        }
    }
    
    private void renderGame() {
        renderLevelBackground();
        
        if (isRunning && !gameEnded) {
            playerBase.render(gc);
            enemyBase.render(gc);
        }
        
        for (GameUnit ally : allies) {
            ally.render(gc, allyImages, enemyImages);
        }
        for (GameUnit enemy : enemies) {
            enemy.render(gc, allyImages, enemyImages);
        }
        
        for (GameProjectile proj : projectiles) {
            proj.render(gc, projectileImages);
        }
        
        if (isPaused) {
            uiRenderer.renderPauseScreen(gc);
        } else if (!isRunning) {
            uiRenderer.renderGameOverScreen(gc, score, wave);
        } else {
            int[] unitCosts = new int[] {40,60,85,150};
            uiRenderer.renderGameUI(gc, energy, selectedUnitType, unitCosts, allyImages,
                    playerBase.health, playerBase.maxHealth, enemyBase.health, enemyBase.maxHealth);
        }
        
        if (!gameEndMessage.isEmpty()) {
            gc.setFill(gameEndColor);
            gc.setFont(javafx.scene.text.Font.font("Arial", 48));
            
            javafx.scene.text.Text tempText = new javafx.scene.text.Text(gameEndMessage);
            tempText.setFont(javafx.scene.text.Font.font("Arial", 48));
            double textWidth = tempText.getBoundsInLocal().getWidth();
            
            gc.fillText(gameEndMessage, (CANVAS_WIDTH - textWidth) / 2, CANVAS_HEIGHT / 2);
        }
    }
    
    private void renderLevelBackground() {
        String backgroundFile = getBackgroundForLevel(currentLevel);
        try {
            javafx.scene.image.Image battlegroundImage = new javafx.scene.image.Image(backgroundFile);
            gc.drawImage(battlegroundImage, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        } catch (Exception e) {
            renderFallback();
        }
    }
    
    private void renderFallback() {
        gc.setFill(Color.web("#0a0a0a"));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        gc.setStroke(Color.web("#2d1b69", 0.3));
        gc.setLineWidth(1);
        for (int i = 0; i < CANVAS_WIDTH; i += 100) {
            gc.strokeLine(i, 0, i, CANVAS_HEIGHT);
        }
        for (int i = 0; i < CANVAS_HEIGHT; i += 100) {
            gc.strokeLine(0, i, CANVAS_WIDTH, i);
        }
    }
    
    private String getBackgroundForLevel(int level) {
        return switch (level) {
            case 1 -> "file:resources/images/backgrounds/City_of_Tears_background.png";
            case 2 -> "file:resources/images/backgrounds/void_arena_battle.png";
            case 3 -> "file:resources/images/backgrounds/nightmare_background.jpg";
            default -> "file:resources/images/backgrounds/void_arena_battle.png";
        };
    }
    
    private BasePosition getBasePositionsForLevel(int level) {
        return switch (level) {
            case 1 -> {
                double playerX = 150;
                double enemyX = CANVAS_WIDTH - 120;
                double floorY = CANVAS_HEIGHT - 90;
                yield new BasePosition(playerX, enemyX, floorY);
            }
            case 2 -> {
                double playerX = 80;
                double enemyX = CANVAS_WIDTH - 80;
                double floorY = CANVAS_HEIGHT - 80;
                yield new BasePosition(playerX, enemyX, floorY);
            }
            case 3 -> {
                double playerX = 80;
                double enemyX = CANVAS_WIDTH - 80;
                double floorY = CANVAS_HEIGHT - 80;
                yield new BasePosition(playerX, enemyX, floorY);
            }
            default -> {
                double playerX = 80;
                double enemyX = CANVAS_WIDTH - 80;
                double floorY = CANVAS_HEIGHT - 100;
                yield new BasePosition(playerX, enemyX, floorY);
            }
        };
    }
    
    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            audioManager.pauseMusic();
        } else {
            audioManager.resumeMusic();
        }
    }
    
    private void restartGame() {
        initializeGame();
    }
    
    private void returnToMenu() {
        System.out.println("Retour au menu de sélection...");
        
        // Arrêter le jeu actuel
        isRunning = false;
        isPaused = false;
        gameEnded = true;
        
        // Arrêter l'audio
        audioManager.stopCurrentMusic();
        
        // Arrêter la boucle de jeu
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        // Retourner au menu de sélection
        demo.menus.LevelSelectMenu levelMenu = new demo.menus.LevelSelectMenu();
        levelMenu.start((javafx.stage.Stage) canvas.getScene().getWindow());
    }
    
    private void stopGame() {
        audioManager.stopCurrentMusic();
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}