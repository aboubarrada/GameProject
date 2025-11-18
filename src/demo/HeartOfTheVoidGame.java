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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HeartOfTheVoidGame {
    
    private static final int CANVAS_WIDTH = 1000;
    private static final int CANVAS_HEIGHT = 600;
    private static final int INITIAL_ENERGY = 100;
    private static final int MAX_ENERGY = 200;
    
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private boolean isRunning = false;
    private boolean isPaused = false;
    
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
    
    private Random random = new Random();
    private Image moneyImage;
    
    private Image[] allyImages = new Image[4];
    private Image[] enemyImages = new Image[4];
    private Image[] projectileImages = new Image[3];
    
    private Stage gameStage;
    
    public void start(Stage stage) {
        this.gameStage = stage;
        setupUI(stage);
        initializeGame();
        startGameLoop();
    }
    
    private void setupUI(Stage stage) {
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        try {
            moneyImage = new Image("file:resources/images/ui/Money.png");
            
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
            
            System.out.println("✅ Toutes les images chargées avec succès!");
        } catch (Exception e) {
            System.out.println("❌ Erreur chargement images: " + e.getMessage());
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
        });
        
        scene.getRoot().setFocusTraversable(true);
        scene.getRoot().requestFocus();
        
        stage.setTitle("🎮 Heart of the Void - Demo Jouable");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> stopGame());
    }
    
    private void initializeGame() {
        int floorY = CANVAS_HEIGHT - 100;
        playerBase = new GameBase(80, floorY, true);
        enemyBase = new GameBase(CANVAS_WIDTH-80, floorY, false);
        
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
    }
    
    private void handleMouseClick(MouseEvent event) {
        if (!isRunning || isPaused) return;
        
        double x = event.getX();
        double y = event.getY();
        int floorY = CANVAS_HEIGHT - 100;
        int floorTolerance = 40;
        
        if (x > 150 && x < CANVAS_WIDTH * 0.6 && 
            y > floorY - floorTolerance && y < floorY + floorTolerance) {
            placeAlly(x, floorY);
        }
    }
    
    private void placeAlly(double x, double y) {
        int cost = getAllyCost(selectedUnitType);
        if (energy >= cost) {
            GameUnit ally = createAlly(selectedUnitType, x, y);
            if (ally != null) {
                allies.add(ally);
                energy -= cost;
            }
        }
    }
    
    private int getAllyCost(int type) {
        return switch (type) {
            case 1 -> 25;
            case 2 -> 35;
            case 3 -> 45;
            case 4 -> 65;
            default -> 25;
        };
    }
    
    private GameUnit createAlly(int type, double x, double y) {
        return switch (type) {
            case 1 -> new GameUnit(x, y, true, 80, 25, 1.5, 60, Color.SILVER, "🛡️Knight", 0);
            case 2 -> new GameUnit(x, y, true, 60, 30, 2.0, 80, Color.PURPLE, "⚫Vessel", 1);
            case 3 -> new GameUnit(x, y, true, 50, 35, 2.5, 120, Color.HOTPINK, "🗡️Hornet", 2);
            case 4 -> new GameUnit(x, y, true, 120, 50, 3.0, 100, Color.GOLD, "✨GodVoid", 3);
            default -> null;
        };
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
                
                if (!isPaused && isRunning) {
                    updateGame(deltaTime);
                }
                renderGame();
            }
        };
        gameLoop.start();
    }
    
    private void updateGame(double deltaTime) {
        gameTime += deltaTime;
        
        energy = Math.min(MAX_ENERGY, energy + (int)(15 * deltaTime));
        
        double baseInterval = wave <= 3 ? 2.5 : wave <= 6 ? 2.0 : wave <= 10 ? 1.8 : 1.5;
        double spawnInterval = Math.max(1.0, baseInterval - difficultyMultiplier * 0.2);
        
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
        int floorY = CANVAS_HEIGHT - 100;
        
        int enemyType = determineEnemyType();
        int baseHealth = getBaseEnemyHealth(enemyType);
        int baseDamage = getBaseEnemyDamage(enemyType);
        double baseSpeed = getBaseEnemySpeed(enemyType);
        double baseRange = getBaseEnemyRange(enemyType);
        String enemyName = getEnemyName(enemyType);
        Color enemyColor = getEnemyColor(enemyType);
        
        int scaledHealth = (int)(baseHealth * difficultyMultiplier);
        int scaledDamage = (int)(baseDamage * difficultyMultiplier);
        
        GameUnit enemy = new GameUnit(CANVAS_WIDTH-80, floorY, false, 
            scaledHealth, scaledDamage, baseSpeed, baseRange, enemyColor, enemyName, enemyType - 1);
        
        enemies.add(enemy);
    }
    
    private int determineEnemyType() {
        double rand = random.nextDouble();
        
        if (wave <= 3) {
            return rand < 0.8 ? 1 : 2;
        } else if (wave <= 6) {
            if (rand < 0.5) return 1;
            else if (rand < 0.8) return 2;
            else return 3;
        } else if (wave <= 10) {
            if (rand < 0.3) return 1;
            else if (rand < 0.6) return 2;
            else if (rand < 0.85) return 3;
            else return 4;
        } else {
            if (rand < 0.2) return 1;
            else if (rand < 0.4) return 2;
            else if (rand < 0.7) return 3;
            else return 4;
        }
    }
    
    private int getBaseEnemyHealth(int type) {
        return switch (type) {
            case 1 -> 50;
            case 2 -> 75;
            case 3 -> 35;
            case 4 -> 150;
            default -> 50;
        };
    }
    
    private int getBaseEnemyDamage(int type) {
        return switch (type) {
            case 1 -> 15;
            case 2 -> 20;
            case 3 -> 25;
            case 4 -> 35;
            default -> 15;
        };
    }
    
    private double getBaseEnemySpeed(int type) {
        return switch (type) {
            case 1 -> 1.2;
            case 2 -> 1.0;
            case 3 -> 2.5;
            case 4 -> 0.8;
            default -> 1.0;
        };
    }
    
    private double getBaseEnemyRange(int type) {
        return switch (type) {
            case 1 -> 45;
            case 2 -> 50;
            case 3 -> 60;
            case 4 -> 80;
            default -> 50;
        };
    }
    
    private String getEnemyName(int type) {
        return switch (type) {
            case 1 -> "💀Husk";
            case 2 -> "🗡️Vessel";
            case 3 -> "🦋Vengefly";
            case 4 -> "☀️Radiance";
            default -> "💀Husk";
        };
    }
    
    private Color getEnemyColor(int type) {
        return switch (type) {
            case 1 -> Color.DARKRED;
            case 2 -> Color.DARKRED;
            case 3 -> Color.ORANGERED;
            case 4 -> Color.YELLOW;
            default -> Color.DARKRED;
        };
    }
    
    private int getEnemyReward(int type) {
        int baseReward = switch (type) {
            case 1 -> 8;
            case 2 -> 12;
            case 3 -> 15;
            case 4 -> 25;
            default -> 8;
        };
        return (int)(baseReward * (1 + wave * 0.1));
    }
    
    private void progressToNextWave() {
        wave++;
        enemiesKilledThisWave = 0;
        enemiesNeededForNextWave = 10 + wave * 2;
        difficultyMultiplier += 0.15;
        
        energy = Math.min(MAX_ENERGY, energy + 30);
        System.out.println("🌊 Vague " + wave + " ! Difficulté: " + String.format("%.1f", difficultyMultiplier));
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
                    int reward = getEnemyReward(unit.unitType + 1);
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
            GameUnit target = findClosestTarget(unit, targets);
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
                        int projType = isAlly ? (unit.unitType == 2 ? 1 : 0) : 2;
                        GameProjectile proj = new GameProjectile(
                            unit.x, unit.y, target.x, target.y, 
                            unit.damage, isAlly ? Color.CYAN : Color.RED, projType
                        );
                        projectiles.add(proj);
                        unit.resetAttackCooldown();
                    }
                } else {
                    unit.moveTowards(target.x, target.y, deltaTime);
                }
            }
        }
    }
    
    private GameUnit findClosestTarget(GameUnit unit, List<GameUnit> targets) {
        GameUnit closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (GameUnit target : targets) {
            double distance = unit.distanceTo(target.x, target.y);
            if (distance < minDistance && distance <= unit.range * 1.5) {
                minDistance = distance;
                closest = target;
            }
        }
        
        return closest;
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
            isRunning = false;
        } else if (!enemyBase.isAlive()) {
            wave++;
            enemyBase = new GameBase(CANVAS_WIDTH-50, CANVAS_HEIGHT/2, false);
            enemyBase.health = enemyBase.maxHealth = 300 + wave * 100;
            energy = Math.min(MAX_ENERGY, energy + 50);
            score += 100;
        }
    }
    
    private void renderGame() {
        try {
            javafx.scene.image.Image battlegroundImage = new javafx.scene.image.Image("file:resources/images/backgrounds/void_arena_battle.png");
            gc.drawImage(battlegroundImage, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        } catch (Exception e) {
            gc.setFill(Color.web("#0a0a0a"));
            gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            
            gc.setStroke(Color.web("#2d1b69", 0.3));
            gc.setLineWidth(1);
            for (int i = 0; i < CANVAS_WIDTH; i += 50) {
                gc.strokeLine(i, 0, i, CANVAS_HEIGHT);
            }
            for (int i = 0; i < CANVAS_HEIGHT; i += 50) {
                gc.strokeLine(0, i, CANVAS_WIDTH, i);
            }
        }
        
        playerBase.render(gc);
        enemyBase.render(gc);
        
        for (GameUnit ally : allies) {
            ally.render(gc, allyImages, enemyImages);
        }
        for (GameUnit enemy : enemies) {
            enemy.render(gc, allyImages, enemyImages);
        }
        
        for (GameProjectile proj : projectiles) {
            proj.render(gc, projectileImages);
        }
        
        renderUI();
    }
    
    private void renderUI() {
        if (moneyImage != null) {
            gc.setFill(Color.web("#1a1a2e", 0.8));
            gc.fillRoundRect(5, 5, 200, 50, 8, 8);
            
            gc.drawImage(moneyImage, 10, 10, 40, 40);
            gc.setFill(Color.GOLD);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            gc.fillText(": " + energy + "/" + MAX_ENERGY, 55, 35);
        } else {
            gc.setFill(Color.web("#1a1a2e"));
            gc.fillRoundRect(10, 10, 220, 30, 5, 5);
            
            double energyPercent = (double) energy / MAX_ENERGY;
            gc.setFill(Color.web("#9d4edd"));
            gc.fillRoundRect(12, 12, 216 * energyPercent, 26, 3, 3);
            
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("💰 Money: " + energy + "/" + MAX_ENERGY, 15, 30);
        }
        
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("🌊 Vague: " + wave + " (Diff: " + String.format("%.1f", difficultyMultiplier) + ")", 10, 70);
        gc.fillText("🏆 Score: " + score, 10, 90);
        
        gc.setFill(Color.LIGHTBLUE);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("🎯 Unité: " + getUnitName(selectedUnitType), 10, 110);
        gc.fillText("👥 Alliés: " + allies.size(), 10, 125);
        gc.fillText("👹 Ennemis: " + enemies.size(), 10, 140);
        
        gc.setFill(Color.ORANGE);
        gc.fillText("💀 Tués: " + enemiesKilledThisWave + "/" + enemiesNeededForNextWave, 10, 155);
        
        gc.setFill(Color.web("#ADD8E6", 0.8));
        gc.setFont(Font.font("Arial", 10));
        gc.fillText("💡 Touches 1-4: Sélection | Espace: Pause | R: Restart", 10, 175);
        gc.fillText("💡 Zone de placement des alliés - Cliquez pour placer!", 100, CANVAS_HEIGHT - 10);
        
        if (isPaused) {
            gc.setFill(Color.web("#000000", 0.7));
            gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            gc.fillText("⏸️ PAUSE", CANVAS_WIDTH/2 - 80, CANVAS_HEIGHT/2);
            
            gc.setFont(Font.font("Arial", 16));
            gc.fillText("Appuyez sur ESPACE pour reprendre", CANVAS_WIDTH/2 - 120, CANVAS_HEIGHT/2 + 40);
        }
        
        if (!isRunning) {
            gc.setFill(Color.web("#000000", 0.8));
            gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            
            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            gc.fillText("💀 GAME OVER", CANVAS_WIDTH/2 - 150, CANVAS_HEIGHT/2);
            
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 16));
            gc.fillText("🏆 Score final: " + score + " | 🌊 Vagues: " + (wave-1), CANVAS_WIDTH/2 - 100, CANVAS_HEIGHT/2 + 40);
            gc.fillText("Appuyez sur R pour recommencer", CANVAS_WIDTH/2 - 90, CANVAS_HEIGHT/2 + 60);
        }
    }
    
    private String getUnitName(int type) {
        return switch (type) {
            case 1 -> "🛡️ The Knight";
            case 2 -> "⚫ Void Vessel";
            case 3 -> "🗡️ Hornet";
            case 4 -> "✨ God Void";
            default -> "❓ Unknown";
        };
    }
    
    private void togglePause() {
        isPaused = !isPaused;
    }
    
    private void restartGame() {
        initializeGame();
    }
    
    private void stopGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}

class GameUnit {
    double x, y;
    int health, maxHealth, damage;
    double attackSpeed, range;
    Color color;
    String name;
    boolean isAlly;
    int unitType;
    double attackCooldown = 0;
    double speed = 30;
    
    GameUnit(double x, double y, boolean isAlly, int health, int damage, double attackSpeed, double range, Color color, String name, int unitType) {
        this.x = x;
        this.y = y;
        this.isAlly = isAlly;
        this.unitType = unitType;
        this.health = this.maxHealth = health;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.range = range;
        this.color = color;
        this.name = name;
        this.speed = isAlly ? 25 : 35;
    }
    
    void update(double deltaTime) {
        if (attackCooldown > 0) {
            attackCooldown -= deltaTime;
        }
    }
    
    void moveTowards(double targetX, double targetY, double deltaTime) {
        double dx = targetX - x;
        double distance = Math.abs(dx);
        int floorY = 600 - 100;
        
        if (distance > 15) {
            double moveDistance = speed * deltaTime;
            if (dx > 0) {
                x += moveDistance;
            } else if (dx < 0) {
                x -= moveDistance;
            }
            
            y = floorY;
            x = Math.max(10, Math.min(1000-10, x));
        }
    }
    
    double distanceTo(double otherX, double otherY) {
        double dx = x - otherX;
        double dy = y - otherY;
        return Math.sqrt(dx*dx + dy*dy);
    }
    
    boolean canAttack() {
        return attackCooldown <= 0;
    }
    
    void resetAttackCooldown() {
        attackCooldown = 1.0 / attackSpeed;
    }
    
    void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }
    
    boolean isAlive() {
        return health > 0;
    }
    
    void render(GraphicsContext gc, Image[] allyImages, Image[] enemyImages) {
        gc.setFill(Color.web("#000000", 0.3));
        gc.fillOval(x-8, y+10, 16, 8);
        
        Image unitImage = null;
        if (isAlly && allyImages != null && unitType < allyImages.length && allyImages[unitType] != null) {
            unitImage = allyImages[unitType];
        } else if (!isAlly && enemyImages != null && unitType < enemyImages.length && enemyImages[unitType] != null) {
            unitImage = enemyImages[unitType];
        }
        
        if (unitImage != null) {
            gc.drawImage(unitImage, x-20, y-20, 40, 40);
        } else {
            if (isAlly) {
                gc.setFill(Color.web("#6a0dad", 0.3));
                gc.fillOval(x-15, y-15, 30, 30);
            } else {
                gc.setFill(Color.web("#8b0000", 0.3));
                gc.fillOval(x-15, y-15, 30, 30);
            }
            
            gc.setFill(color);
            gc.fillOval(x-12, y-12, 24, 24);
            
            gc.setStroke(isAlly ? Color.WHITE : Color.DARKRED);
            gc.setLineWidth(2);
            gc.strokeOval(x-12, y-12, 24, 24);
        }
        
        if (health < maxHealth) {
            gc.setFill(Color.web("#2c2c2c"));
            gc.fillRoundRect(x-15, y-25, 30, 6, 3, 3);
            
            double healthPercent = (double) health / maxHealth;
            Color healthColor = healthPercent > 0.6 ? Color.GREEN : 
                               healthPercent > 0.3 ? Color.YELLOW : Color.RED;
            gc.setFill(healthColor);
            gc.fillRoundRect(x-15, y-25, 30 * healthPercent, 6, 3, 3);
        }
        
        if (canAttack() && range > 0) {
            gc.setStroke(Color.web(color.toString(), 0.2));
            gc.setLineWidth(1);
            gc.strokeOval(x-range, y-range, range*2, range*2);
        }
    }
}

class GameBase {
    double x, y;
    int health, maxHealth;
    boolean isPlayerBase;
    
    GameBase(double x, double y, boolean isPlayerBase) {
        this.x = x;
        this.y = y;
        this.isPlayerBase = isPlayerBase;
        this.health = this.maxHealth = isPlayerBase ? 500 : 300;
    }
    
    void update(double deltaTime) {
        if (isPlayerBase && health < maxHealth) {
            health = Math.min(maxHealth, health + 1);
        }
    }
    
    void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }
    
    boolean isAlive() {
        return health > 0;
    }
    
    void render(GraphicsContext gc) {
        Color baseColor = isPlayerBase ? Color.web("#4169e1") : Color.web("#8b0000");
        
        gc.setFill(Color.web("#000000", 0.4));
        gc.fillRect(x-22, y-38, 44, 88);
        
        gc.setFill(baseColor);
        gc.fillRect(x-20, y-40, 40, 80);
        
        gc.setFill(Color.web(baseColor.toString()).darker());
        gc.fillRect(x-18, y-35, 36, 5);
        gc.fillRect(x-18, y-15, 36, 5);
        gc.fillRect(x-18, y+5, 36, 5);
        gc.fillRect(x-18, y+25, 36, 5);
        
        if (isPlayerBase) {
            gc.setFill(Color.PURPLE);
        } else {
            gc.setFill(Color.ORANGE);
        }
        gc.fillPolygon(new double[]{x-10, x, x+10, x}, 
                      new double[]{y-45, y-55, y-45, y-40}, 4);
        
        gc.setFill(Color.web("#2c2c2c"));
        gc.fillRoundRect(x-30, y-60, 60, 8, 4, 4);
        
        double healthPercent = (double) health / maxHealth;
        Color healthColor = healthPercent > 0.6 ? Color.GREEN : 
                           healthPercent > 0.3 ? Color.YELLOW : Color.RED;
        gc.setFill(healthColor);
        gc.fillRoundRect(x-30, y-60, 60 * healthPercent, 8, 4, 4);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText(health + "/" + maxHealth, x-20, y-65);
        
        gc.setFill(isPlayerBase ? Color.LIGHTBLUE : Color.ORANGE);
        gc.setFont(Font.font("Arial", 10));
        String baseName = isPlayerBase ? "🏰 Base Alliée" : "🏴 Base Ennemie";
        gc.fillText(baseName, x-25, y+50);
    }
}

class GameProjectile {
    double x, y, targetX, targetY;
    double speed = 250;
    int damage;
    Color color;
    int projectileType;
    double lifetime = 4.0;
    double age = 0;
    
    GameProjectile(double startX, double startY, double targetX, double targetY, int damage, Color color, int projectileType) {
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.damage = damage;
        this.color = color;
        this.projectileType = projectileType;
    }
    
    void update(double deltaTime) {
        age += deltaTime;
        
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx*dx + dy*dy);
        
        if (distance > 3) {
            double moveDistance = speed * deltaTime;
            x += (dx / distance) * moveDistance;
            y += (dy / distance) * moveDistance;
        }
    }
    
    boolean isExpired() {
        return age >= lifetime;
    }
    
    boolean collidesWith(GameUnit unit) {
        double distance = Math.sqrt((x - unit.x)*(x - unit.x) + (y - unit.y)*(y - unit.y));
        return distance < 18;
    }
    
    void render(GraphicsContext gc, Image[] projectileImages) {
        if (projectileImages != null && projectileType < projectileImages.length && projectileImages[projectileType] != null) {
            gc.drawImage(projectileImages[projectileType], x-8, y-8, 16, 16);
        } else {
            gc.setStroke(Color.web(color.toString(), 0.3));
            gc.setLineWidth(3);
            gc.strokeLine(x-5, y, x+5, y);
            
            gc.setFill(color);
            gc.fillOval(x-4, y-4, 8, 8);
            
            gc.setFill(Color.web(color.toString(), 0.5));
            gc.fillOval(x-6, y-6, 12, 12);
        }
    }
}