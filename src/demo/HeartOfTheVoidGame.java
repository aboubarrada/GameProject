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

// Stocke les positions des bases
class BasePosition {
    double playerX, enemyX, floorY;
    
    BasePosition(double playerX, double enemyX, double floorY) {
        this.playerX = playerX;
        this.enemyX = enemyX;
        this.floorY = floorY;
    }
}

// Classe principale du jeu (boucle de jeu et combat)
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
    private int score = 0;
    private int selectedUnitType = 1;
    private double lastEnemySpawn = 0;
    private double gameTime = 0;
    private double lastMoneyRegen = 0;
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
    
    /**
     * Démarre le jeu avec le niveau sélectionné.
     * @param stage La fenêtre JavaFX
     */
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
    
    /**
     * Définit le niveau à jouer (1, 2 ou 3).
     * @param level Le numéro du niveau
     */
    public void setLevel(int level) {
        this.currentLevel = level;
    }
    
    /**
     * Configure l'interface graphique et charge les images.
     * @param stage La fenêtre du jeu
     */
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
        } catch (Exception e) {
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
    
    /**
     * Initialise une nouvelle partie : crée les bases, reset l'énergie, vide les listes.
     */
    private void initializeGame() {
        BasePosition positions = getBasePositionsForLevel(currentLevel);
        
        playerBase = new GameBase(positions.playerX, positions.floorY, true);
        enemyBase = new GameBase(positions.enemyX, positions.floorY, false);
        
        if (currentLevel == 1) {
            playerBase.health = playerBase.maxHealth = 500;
            enemyBase.health = enemyBase.maxHealth = 400;
        } else if (currentLevel == 2) {
            playerBase.health = playerBase.maxHealth = 400;
            enemyBase.health = enemyBase.maxHealth = 600;
        } else if (currentLevel == 3) {
            playerBase.health = playerBase.maxHealth = 1;
            enemyBase.health = enemyBase.maxHealth = 800;
        }
        
        allies.clear();
        enemies.clear();
        projectiles.clear();
        
        energy = INITIAL_ENERGY;
        score = 0;
        gameTime = 0;
        lastEnemySpawn = 0;
        lastMoneyRegen = 0;
        difficultyMultiplier = 1.0;
        isRunning = true;
        isPaused = false;
        
        gameEnded = false;
        gameEndMessage = "";
        gameEndColor = Color.WHITE;
    }
    
    /**
     * Gère le clic de souris pour sélectionner et déployer des unités.
     * @param event L'événement de clic
     */
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
                    placeAllyFromBase();
                    return;
                }
            }
        }
    }
    
    private void placeAllyFromBase() {
        int cost = gameManager.getAllyCost(selectedUnitType);
        if (energy >= cost) {
            BasePosition positions = getBasePositionsForLevel(currentLevel);
            GameUnit ally = gameManager.createAlly(selectedUnitType, positions.playerX + 40, positions.floorY);
            if (ally != null) {
                allies.add(ally);
                energy -= cost;
            }
        };
    }
    
    // Lance la boucle de jeu principale
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
    
    // Met à jour la logique du jeu (ennemis, combats, projectiles)
    private void updateGame(double deltaTime) {
        gameTime += deltaTime;
        
        difficultyMultiplier = 1.0 + (gameTime / 60.0);
        
        if (gameTime - lastMoneyRegen > 2.0) {
            energy = Math.min(MAX_ENERGY, energy + 5);
            lastMoneyRegen = gameTime;
        }
        
        double spawnInterval = Math.max(0.8, 3.0 - (gameTime / 120.0));
        
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
        int enemyType = 1 + (int)(Math.random() * Math.min(4, 1 + gameTime / 30.0));
        GameUnit enemy = gameManager.createEnemyAtPosition(enemyType, difficultyMultiplier, positions.enemyX - 40, positions.floorY);
        enemies.add(enemy);
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
                    int reward = gameManager.getEnemyReward(unit.unitType, 1);
                    energy += reward;
                    score += reward * 2;
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
    
    // Met à jour tous les projectiles
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
                gameEndMessage = "GAME OVER";
                gameEndColor = Color.RED;
                
                allies.clear();
                enemies.clear();
                projectiles.clear();
                
                if (gameLoop != null) {
                    gameLoop.stop();
                }
            }
            
        } else if (!enemyBase.isAlive() && isRunning) {
            if (!gameEnded) {
                isRunning = false;
                gameEnded = true;
                gameEndMessage = "VICTOIRE";
                gameEndColor = Color.GOLD;
                
                audioManager.stopCurrentMusic();
                
                if (gameLoop != null) {
                    gameLoop.stop();
                }
                
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> {
                        returnToMenu();
                    })
                );
                timeline.play();
            }
        }
    }
    
    // Dessine tout le jeu à l'écran
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
        } else if (!isRunning && gameEndMessage.equals("GAME OVER")) {
            uiRenderer.renderGameOverScreen(gc, score, (int)gameTime);
        } else if (isRunning) {
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
            case 1 -> "file:resources/images/backgrounds/void_arena_battle.png";
            case 2 -> "file:resources/images/backgrounds/City_of_Tears_background.png";
            case 3 -> "file:resources/images/backgrounds/nightmare_background.jpg";
            default -> "file:resources/images/backgrounds/void_arena_battle.png";
        };
    }
    
    // Retourne les positions des bases selon le niveau
    private BasePosition getBasePositionsForLevel(int level) {
        return switch (level) {
            case 1 -> {
                double playerX = 150;
                double enemyX = CANVAS_WIDTH - 150;
                double floorY = CANVAS_HEIGHT - 75;
                yield new BasePosition(playerX, enemyX, floorY);
            }
            case 2 -> {
                double playerX = 150;
                double enemyX = CANVAS_WIDTH - 150;
                double floorY = CANVAS_HEIGHT - 75;
                yield new BasePosition(playerX, enemyX, floorY);
            }
            case 3 -> {
                double playerX = 150;
                double enemyX = CANVAS_WIDTH - 150;
                double floorY = CANVAS_HEIGHT - 80;
                yield new BasePosition(playerX, enemyX, floorY);
            }
            default -> {
                double playerX = 150;
                double enemyX = CANVAS_WIDTH - 150;
                double floorY = CANVAS_HEIGHT - 80;
                yield new BasePosition(playerX, enemyX, floorY);
            }
        };
    }
    
    // Met le jeu en pause ou le reprend
    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            audioManager.pauseMusic();
        } else {
            audioManager.resumeMusic();
        }
    }
    
    // Recommence le niveau actuel
    private void restartGame() {
        initializeGame();
    }
    
    // Retourne au menu principal
    private void returnToMenu() {
        isRunning = false;
        isPaused = false;
        gameEnded = true;
        
        audioManager.stopCurrentMusic();
        
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        try {
            Class<?> levelSelectClass = Class.forName("demo.menus.LevelSelectMenu");
            Object levelMenu = levelSelectClass.getDeclaredConstructor().newInstance();
            
            java.lang.reflect.Method startMethod = levelSelectClass.getMethod("start", Stage.class);
            startMethod.invoke(levelMenu, (javafx.stage.Stage) canvas.getScene().getWindow());
            
        } catch (Exception e) {
            e.printStackTrace();
            ((javafx.stage.Stage) canvas.getScene().getWindow()).close();
        }
    }
    
    // Arrête complètement le jeu
    private void stopGame() {
        audioManager.stopCurrentMusic();
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}