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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HeartOfTheVoidGame extends Application {
    
    private static final int CANVAS_WIDTH = 1200;
    private static final int CANVAS_HEIGHT = 700;
    private static final int INITIAL_ENERGY = 150;
    private static final int MAX_ENERGY = 300;
    
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
    private double lastEnergyGain = 0;
    private double lastWaveSpawn = 0;
    
    // Image de background
    private Image backgroundImage;
    
    // Systèmes de particules
    private List<ParticleSystem> particleSystems = new ArrayList<>();
    
    // Files thread-safe pour les nouvelles entités
    private ConcurrentLinkedQueue<GameUnit> newAllies = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<GameUnit> newEnemies = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<GameProjectile> newProjectiles = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<PowerUp> newPowerUps = new ConcurrentLinkedQueue<>();
    
    private List<GameUnit> allies = new ArrayList<>();
    private List<GameUnit> enemies = new ArrayList<>();
    private List<GameProjectile> projectiles = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<SpecialEffect> specialEffects = new ArrayList<>();
    
    private GameBase playerBase;
    private GameBase enemyBase;
    
    private Random random = new Random();
    private ExecutorService threadPool = Executors.newFixedThreadPool(2);
    
    private Stage gameStage;
    private boolean showRangeIndicator = false;
    private boolean showHitboxes = false;
    private double mouseX, mouseY;
    
    // Améliorations de performance
    private long frameCount = 0;
    private double fps = 60;
    private long lastFpsUpdate = 0;
    
    // Nouvelles mécaniques
    private int killCount = 0;
    private int combo = 0;
    private double comboTime = 0;
    private boolean godMode = false;
    private double godModeTime = 0;
    private int specialAbilityCharges = 3;
    
    @Override
    public void start(Stage stage) {
        this.gameStage = stage;
        setupUI(stage);
        initializeGame();
        startGameLoop();
    }
    
    private void setupUI(Stage stage) {
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        // Charger l'image de background
        try {
            backgroundImage = new Image("file:resources/images/backgrounds/void_arena_battle.png");
        } catch (Exception e) {
            // Fallback si l'image n'est pas trouvée
            backgroundImage = null;
        }
        
        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnMouseMoved(this::handleMouseMove);
        canvas.setOnMouseDragged(this::handleMouseDrag);
        
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DIGIT1) selectedUnitType = 1;
            else if (e.getCode() == KeyCode.DIGIT2) selectedUnitType = 2;
            else if (e.getCode() == KeyCode.DIGIT3) selectedUnitType = 3;
            else if (e.getCode() == KeyCode.DIGIT4) selectedUnitType = 4;
            else if (e.getCode() == KeyCode.DIGIT5) selectedUnitType = 5;
            else if (e.getCode() == KeyCode.SPACE) togglePause();
            else if (e.getCode() == KeyCode.R) restartGame();
            else if (e.getCode() == KeyCode.TAB) showRangeIndicator = !showRangeIndicator;
            else if (e.getCode() == KeyCode.H) showHitboxes = !showHitboxes;
            else if (e.getCode() == KeyCode.Q) useSpecialAbility();
            else if (e.getCode() == KeyCode.G) toggleGodMode();
            else if (e.getCode() == KeyCode.F) spawnFriendlyWave();
        });
        
        scene.getRoot().setFocusTraversable(true);
        scene.getRoot().requestFocus();
        
        stage.setTitle("🎮 Heart of the Void - ULTIMATE EDITION");
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
        powerUps.clear();
        particleSystems.clear();
        specialEffects.clear();
        
        newAllies.clear();
        newEnemies.clear();
        newProjectiles.clear();
        newPowerUps.clear();
        
        createInitialUnits();
        
        energy = INITIAL_ENERGY;
        wave = 1;
        score = 0;
        killCount = 0;
        combo = 0;
        gameTime = 0;
        lastEnemySpawn = 0;
        lastEnergyGain = 0;
        lastWaveSpawn = 0;
        isRunning = true;
        isPaused = false;
        godMode = false;
        specialAbilityCharges = 3;
    }
    
    private void createInitialUnits() {
        // Unités de départ gratuites
        int floorY = CANVAS_HEIGHT - 100;
        allies.add(createAlly(1, 150, floorY));
        allies.add(createAlly(2, 200, floorY));
    }
    
    private void handleMouseClick(MouseEvent event) {
        if (!isRunning || isPaused) return;
        
        double x = event.getX();
        double y = event.getY();
        int floorY = CANVAS_HEIGHT - 100;
        int floorTolerance = 40;
        
        // Vérifier si on clique sur un power-up
        Iterator<PowerUp> powerUpIt = powerUps.iterator();
        while (powerUpIt.hasNext()) {
            PowerUp powerUp = powerUpIt.next();
            if (powerUp.contains(x, y)) {
                applyPowerUp(powerUp);
                powerUpIt.remove();
                createParticleSystem(x, y, 20, powerUp.getColor(), 1.0);
                return;
            }
        }
        
        // Placement d'unité rapide avec double-clic
        if (event.getClickCount() == 2 && x > 150 && x < CANVAS_WIDTH * 0.7 && 
            y > floorY - floorTolerance && y < floorY + floorTolerance) {
            
            placeAlly(x, floorY);
            createParticleSystem(x, floorY, 15, Color.CYAN, 0.5);
        }
    }
    
    private void handleMouseMove(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
    }
    
    private void handleMouseDrag(MouseEvent event) {
        // Placement rapide en drag
        if (!isRunning || isPaused) return;
        
        double x = event.getX();
        double y = event.getY();
        int floorY = CANVAS_HEIGHT - 100;
        int floorTolerance = 40;
        
        if (x > 150 && x < CANVAS_WIDTH * 0.7 && 
            y > floorY - floorTolerance && y < floorY + floorTolerance) {
            
            if (energy >= getAllyCost(selectedUnitType)) {
                placeAlly(x, floorY);
            }
        }
    }
    
    private void placeAlly(double x, double y) {
        int cost = getAllyCost(selectedUnitType);
        if (energy >= cost) {
            GameUnit ally = createAlly(selectedUnitType, x, y);
            if (ally != null) {
                newAllies.offer(ally);
                energy -= cost;
                
                // Effet visuel
                createParticleSystem(x, y, 12, ally.color, 0.3);
            }
        }
    }
    
    private int getAllyCost(int type) {
        return switch (type) {
            case 1 -> 20;
            case 2 -> 30;
            case 3 -> 40;
            case 4 -> 50;
            case 5 -> 80;
            default -> 20;
        };
    }
    
    private GameUnit createAlly(int type, double x, double y) {
        return switch (type) {
            case 1 -> new GameUnit(x, y, true, 120, 25, 2.0, 80, Color.SILVER, "🛡️Knight", 35);
            case 2 -> new GameUnit(x, y, true, 80, 40, 2.5, 100, Color.PURPLE, "⚫Vessel", 30);
            case 3 -> new GameUnit(x, y, true, 70, 45, 3.0, 120, Color.HOTPINK, "🗡️Hornet", 45);
            case 4 -> new GameUnit(x, y, true, 180, 70, 4.0, 150, Color.GOLD, "✨GodVoid", 25);
            case 5 -> new GameUnit(x, y, true, 100, 60, 5.0, 180, Color.CYAN, "⚡Speedster", 60);
            default -> null;
        };
    }
    
    private void useSpecialAbility() {
        if (specialAbilityCharges > 0) {
            specialAbilityCharges--;
            
            // Ability: explosion de zone
            for (GameUnit enemy : enemies) {
                if (enemy.distanceTo(playerBase.x, playerBase.y) < 300) {
                    enemy.takeDamage(100);
                    createParticleSystem(enemy.x, enemy.y, 8, Color.ORANGE, 0.8);
                }
            }
            
            // Effet visuel
            createParticleSystem(playerBase.x, playerBase.y, 50, Color.YELLOW, 1.5);
            specialEffects.add(new SpecialEffect("EXPLOSION", playerBase.x, playerBase.y, 1.0));
        }
    }
    
    private void toggleGodMode() {
        if (energy >= 100) {
            godMode = !godMode;
            godModeTime = 10.0; // 10 secondes de god mode
            energy -= 100;
            
            if (godMode) {
                specialEffects.add(new SpecialEffect("GOD_MODE", playerBase.x, playerBase.y, 10.0));
            }
        }
    }
    
    private void spawnFriendlyWave() {
        if (energy >= 60) {
            energy -= 60;
            int floorY = CANVAS_HEIGHT - 100;
            
            for (int i = 0; i < 3; i++) {
                double x = 150 + i * 50;
                GameUnit ally = createAlly(1 + random.nextInt(3), x, floorY);
                if (ally != null) {
                    newAllies.offer(ally);
                }
            }
        }
    }
    
    private void applyPowerUp(PowerUp powerUp) {
        switch (powerUp.type) {
            case ENERGY:
                energy = Math.min(MAX_ENERGY, energy + 60);
                break;
            case HEAL:
                for (GameUnit ally : allies) {
                    ally.health = Math.min(ally.maxHealth, ally.health + 80);
                }
                break;
            case DAMAGE_BOOST:
                for (GameUnit ally : allies) {
                    ally.damage += 15;
                    ally.attackSpeed *= 1.2;
                }
                specialEffects.add(new SpecialEffect("DAMAGE_BOOST", playerBase.x, playerBase.y, 5.0));
                break;
            case SPEED_BOOST:
                for (GameUnit ally : allies) {
                    ally.speed *= 1.3;
                }
                specialEffects.add(new SpecialEffect("SPEED_BOOST", playerBase.x, playerBase.y, 5.0));
                break;
            case COMBO:
                combo += 5;
                score += 100;
                break;
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
                
                // Calcul du FPS
                frameCount++;
                if (now - lastFpsUpdate >= 1_000_000_000) {
                    fps = frameCount;
                    frameCount = 0;
                    lastFpsUpdate = now;
                }
                
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
        
        // Mise à jour des effets spéciaux
        updateSpecialEffects(deltaTime);
        
        // God mode
        if (godMode) {
            godModeTime -= deltaTime;
            if (godModeTime <= 0) {
                godMode = false;
            }
        }
        
        // Combo system
        if (combo > 0) {
            comboTime -= deltaTime;
            if (comboTime <= 0) {
                combo = 0;
            }
        }
        
        // Gain d'énergie régulier avec bonus de combo
        int energyGain = 6 + (combo / 2);
        if (gameTime - lastEnergyGain > 0.8) {
            energy = Math.min(MAX_ENERGY, energy + energyGain);
            lastEnergyGain = gameTime;
        }
        
        // Vagues d'ennemis plus dynamiques
        updateWaveSystem(deltaTime);
        
        // Apparition aléatoire de power-ups
        if (random.nextDouble() < 0.003) {
            spawnPowerUp();
        }
        
        // Mise à jour des entités
        updateEntities(deltaTime);
        
        // Ajout des nouvelles entités
        addNewEntities();
        
        checkGameEnd();
    }
    
    private void updateWaveSystem(double deltaTime) {
        double baseSpawnInterval = Math.max(0.3, 2.5 - wave * 0.12);
        
        if (gameTime - lastEnemySpawn > baseSpawnInterval) {
            int enemiesToSpawn = 1 + random.nextInt(1 + wave / 3);
            
            for (int i = 0; i < enemiesToSpawn; i++) {
                spawnEnemy();
            }
            
            lastEnemySpawn = gameTime;
        }
        
        // Vagues spéciales
        if (gameTime - lastWaveSpawn > 30.0) {
            spawnSpecialWave();
            lastWaveSpawn = gameTime;
        }
    }
    
    private void spawnEnemy() {
        int floorY = CANVAS_HEIGHT - 100;
        int enemyType = 1 + random.nextInt(Math.min(5, 1 + wave / 2));
        
        // Bonus de stats pour les ennemis basé sur la vague
        double waveMultiplier = 1.0 + (wave - 1) * 0.15;
        
        GameUnit enemy = switch (enemyType) {
            case 1 -> new GameUnit(CANVAS_WIDTH-80, floorY, false, 
                (int)(80 * waveMultiplier), (int)(30 * waveMultiplier), 
                1.4, 70, Color.DARKRED, "💀Husk", 40);
            case 2 -> new GameUnit(CANVAS_WIDTH-80, floorY, false, 
                (int)(120 * waveMultiplier), (int)(25 * waveMultiplier), 
                2.0, 60, Color.DARKRED, "🗡️Vessel", 45);
            case 3 -> new GameUnit(CANVAS_WIDTH-80, floorY, false, 
                (int)(60 * waveMultiplier), (int)(40 * waveMultiplier), 
                3.0, 90, Color.ORANGERED, "🦋Vengefly", 55);
            case 4 -> new GameUnit(CANVAS_WIDTH-80, floorY, false, 
                (int)(300 * waveMultiplier), (int)(60 * waveMultiplier), 
                1.2, 120, Color.YELLOW, "☀️Radiance", 30);
            case 5 -> new GameUnit(CANVAS_WIDTH-80, floorY, false, 
                (int)(150 * waveMultiplier), (int)(50 * waveMultiplier), 
                4.0, 100, Color.DARKORANGE, "🔥Berserker", 50);
            default -> new GameUnit(CANVAS_WIDTH-80, floorY, false, 
                (int)(80 * waveMultiplier), (int)(30 * waveMultiplier), 
                1.4, 70, Color.DARKRED, "💀Husk", 40);
        };
        
        newEnemies.offer(enemy);
    }
    
    private void spawnSpecialWave() {
        int floorY = CANVAS_HEIGHT - 100;
        
        // Vague de boss
        for (int i = 0; i < Math.min(3, 1 + wave / 5); i++) {
            GameUnit boss = new GameUnit(CANVAS_WIDTH-80, floorY - i * 30, false, 
                500 + wave * 50, 80 + wave * 10, 1.5, 150, Color.PURPLE, "👑Void King", 20);
            newEnemies.offer(boss);
        }
        
        specialEffects.add(new SpecialEffect("BOSS_WAVE", enemyBase.x, enemyBase.y, 3.0));
    }
    
    private void spawnPowerUp() {
        int floorY = CANVAS_HEIGHT - 100;
        double x = 200 + random.nextDouble() * (CANVAS_WIDTH - 400);
        double y = floorY - 30 - random.nextDouble() * 100;
        
        PowerUp.Type[] types = PowerUp.Type.values();
        PowerUp.Type type = types[random.nextInt(types.length)];
        
        newPowerUps.offer(new PowerUp(x, y, type));
    }
    
    private void updateEntities(double deltaTime) {
        // Mise à jour séquentielle (plus simple sans obstacles)
        updateUnitList(allies, enemies, deltaTime, true);
        updateUnitList(enemies, allies, deltaTime, false);
        updateProjectiles(deltaTime);
        updateParticleSystems(deltaTime);
        updateBases(deltaTime);
        updatePowerUps(deltaTime);
    }
    
    private void updateUnitList(List<GameUnit> units, List<GameUnit> targets, double deltaTime, boolean isAlly) {
        Iterator<GameUnit> it = units.iterator();
        while (it.hasNext()) {
            GameUnit unit = it.next();
            
            if (!unit.isAlive()) {
                if (!isAlly) {
                    int baseReward = 20 + wave;
                    int killReward = (int)(baseReward * (1 + combo * 0.1));
                    
                    energy += killReward;
                    score += killReward;
                    killCount++;
                    combo++;
                    comboTime = 5.0; // Reset combo timer
                    
                    // Chance de drop un power-up
                    if (random.nextDouble() < 0.15 + combo * 0.02) {
                        spawnPowerUpAt(unit.x, unit.y);
                    }
                    
                    // Effet de mort
                    createParticleSystem(unit.x, unit.y, 15, unit.color, 0.8);
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
                        
                        // Effet d'attaque sur la base
                        createParticleSystem(targetBase.x, targetBase.y, 8, Color.RED, 0.3);
                    }
                }
            } else {
                if (unit.distanceTo(target.x, target.y) <= unit.range) {
                    if (unit.canAttack()) {
                        GameProjectile proj = new GameProjectile(
                            unit.x, unit.y, target.x, target.y, 
                            unit.damage, isAlly ? Color.CYAN : Color.RED,
                            unit.range, unit.attackSpeed
                        );
                        newProjectiles.offer(proj);
                        unit.resetAttackCooldown();
                        
                        // Effet de tir
                        createParticleSystem(unit.x, unit.y, 5, unit.color, 0.2);
                    }
                } else {
                    unit.moveTowards(target.x, target.y, deltaTime);
                }
            }
        }
    }
    
    private void spawnPowerUpAt(double x, double y) {
        PowerUp.Type[] types = PowerUp.Type.values();
        PowerUp.Type type = types[random.nextInt(types.length)];
        newPowerUps.offer(new PowerUp(x, y, type));
    }
    
    private GameUnit findClosestTarget(GameUnit unit, List<GameUnit> targets) {
        GameUnit closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (GameUnit target : targets) {
            double distance = unit.distanceTo(target.x, target.y);
            if (distance < minDistance && distance <= unit.range * 1.8) {
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
            List<GameUnit> targetList = proj.color == Color.CYAN ? enemies : allies;
            
            for (GameUnit target : targetList) {
                if (proj.collidesWith(target)) {
                    target.takeDamage(proj.damage);
                    hit = true;
                    
                    // Effet d'impact
                    createParticleSystem(proj.x, proj.y, 6, proj.color, 0.5);
                    break;
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
        
        // God mode protection
        if (godMode) {
            playerBase.health = Math.min(playerBase.maxHealth, playerBase.health + 10);
        }
    }
    
    private void updatePowerUps(double deltaTime) {
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            PowerUp powerUp = it.next();
            powerUp.update(deltaTime);
            if (powerUp.isExpired()) {
                it.remove();
            }
        }
    }
    
    private void updateParticleSystems(double deltaTime) {
        Iterator<ParticleSystem> it = particleSystems.iterator();
        while (it.hasNext()) {
            ParticleSystem ps = it.next();
            ps.update(deltaTime);
            if (ps.isDead()) {
                it.remove();
            }
        }
    }
    
    private void updateSpecialEffects(double deltaTime) {
        Iterator<SpecialEffect> it = specialEffects.iterator();
        while (it.hasNext()) {
            SpecialEffect effect = it.next();
            effect.update(deltaTime);
            if (effect.isFinished()) {
                it.remove();
            }
        }
    }
    
    private void addNewEntities() {
        // Ajout des nouvelles entités de manière thread-safe
        while (!newAllies.isEmpty()) allies.add(newAllies.poll());
        while (!newEnemies.isEmpty()) enemies.add(newEnemies.poll());
        while (!newProjectiles.isEmpty()) projectiles.add(newProjectiles.poll());
        while (!newPowerUps.isEmpty()) powerUps.add(newPowerUps.poll());
    }
    
    private void createParticleSystem(double x, double y, int count, Color color, double duration) {
        particleSystems.add(new ParticleSystem(x, y, count, color, duration));
    }
    
    private void checkGameEnd() {
        if (!playerBase.isAlive()) {
            isRunning = false;
            specialEffects.add(new SpecialEffect("GAME_OVER", CANVAS_WIDTH/2, CANVAS_HEIGHT/2, 5.0));
        } else if (!enemyBase.isAlive()) {
            wave++;
            enemyBase = new GameBase(CANVAS_WIDTH-80, CANVAS_HEIGHT-100, false);
            enemyBase.health = enemyBase.maxHealth = 500 + wave * 150;
            energy = Math.min(MAX_ENERGY, energy + 100);
            score += 200;
            specialAbilityCharges = Math.min(5, specialAbilityCharges + 1);
            
            // Récompense de vague
            for (GameUnit ally : allies) {
                ally.health = Math.min(ally.maxHealth, ally.health + 60);
                ally.damage += 5;
            }
            
            specialEffects.add(new SpecialEffect("WAVE_CLEAR", CANVAS_WIDTH/2, 100, 3.0));
        }
    }
    
    private void renderGame() {
        // Background simple avec image
        drawBackground();
        
        // Render dans l'ordre de profondeur
        for (PowerUp powerUp : powerUps) {
            powerUp.render(gc);
        }
        
        for (GameUnit ally : allies) {
            ally.render(gc);
        }
        for (GameUnit enemy : enemies) {
            enemy.render(gc);
        }
        
        for (GameProjectile proj : projectiles) {
            proj.render(gc);
        }
        
        for (ParticleSystem ps : particleSystems) {
            ps.render(gc);
        }
        
        for (SpecialEffect effect : specialEffects) {
            effect.render(gc);
        }
        
        playerBase.render(gc);
        enemyBase.render(gc);
        
        renderUI();
        
        // Indicateur de portée
        if (showRangeIndicator) {
            GameUnit sampleUnit = createAlly(selectedUnitType, mouseX, mouseY);
            if (sampleUnit != null) {
                gc.setStroke(Color.web("#00ff00", 0.3));
                gc.setLineWidth(2);
                gc.strokeOval(mouseX - sampleUnit.range, mouseY - sampleUnit.range, 
                             sampleUnit.range * 2, sampleUnit.range * 2);
            }
        }
        
        // Hitboxes debug
        if (showHitboxes) {
            renderHitboxes();
        }
    }
    
    private void drawBackground() {
        if (backgroundImage != null) {
            // Dessiner l'image de background
            gc.drawImage(backgroundImage, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        } else {
            // Fallback si l'image n'est pas disponible
            gc.setFill(Color.web("#0a0a2a"));
            gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            
            // Sol simple
            int floorY = CANVAS_HEIGHT - 100;
            gc.setFill(Color.web("#2d1b69", 0.8));
            gc.fillRect(0, floorY, CANVAS_WIDTH, CANVAS_HEIGHT - floorY);
        }
    }
    
    private void renderHitboxes() {
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(1);
        
        for (GameUnit unit : allies) {
            gc.strokeOval(unit.x - 12, unit.y - 12, 24, 24);
        }
        for (GameUnit unit : enemies) {
            gc.strokeOval(unit.x - 12, unit.y - 12, 24, 24);
        }
    }
    
    private void renderUI() {
        // Barre d'énergie améliorée
        renderEnergyBar();
        
        // Informations de jeu
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("🌊 Vague: " + wave, 10, 60);
        gc.fillText("🏆 Score: " + score, 10, 85);
        gc.fillText("💀 Kills: " + killCount, 10, 110);
        
        // Combo system
        if (combo > 0) {
            gc.setFill(Color.ORANGE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.fillText("🔥 Combo: x" + combo + "!", 10, 135);
        }
        
        // Informations d'unité
        gc.setFill(Color.LIGHTBLUE);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("🎯 " + getUnitName(selectedUnitType), 10, 160);
        gc.fillText("💰 Coût: " + getAllyCost(selectedUnitType), 10, 175);
        gc.fillText("👥 Alliés: " + allies.size(), 10, 190);
        gc.fillText("👹 Ennemis: " + enemies.size(), 10, 205);
        
        // Capacités spéciales
        renderSpecialAbilities();
        
        // FPS et performances
        gc.setFill(Color.LIGHTGREEN);
        gc.setFont(Font.font("Arial", 10));
        gc.fillText(String.format("FPS: %.1f", fps), CANVAS_WIDTH - 60, 20);
        gc.fillText("Entities: " + (allies.size() + enemies.size() + projectiles.size()), CANVAS_WIDTH - 100, 35);
        
        // Instructions
        renderInstructions();
        
        if (isPaused) {
            renderPauseScreen();
        }
        
        if (!isRunning) {
            renderGameOverScreen();
        }
    }
    
    private void renderEnergyBar() {
        double energyPercent = (double) energy / MAX_ENERGY;
        
        // Fond
        gc.setFill(Color.web("#1a1a2e", 0.9));
        gc.fillRoundRect(10, 10, 250, 35, 8, 8);
        
        // Barre d'énergie avec dégradé
        if (energyPercent > 0) {
            Color startColor = energyPercent > 0.3 ? Color.web("#9d4edd") : Color.RED;
            Color endColor = energyPercent > 0.3 ? Color.web("#c77dff") : Color.ORANGE;
            
            for (int i = 0; i < 246 * energyPercent; i++) {
                double ratio = i / (246 * energyPercent);
                Color interpolated = interpolateColor(startColor, endColor, ratio);
                gc.setFill(interpolated);
                gc.fillRect(12 + i, 12, 1, 31);
            }
        }
        
        // Texte
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("⚡ Énergie: " + energy + "/" + MAX_ENERGY, 15, 32);
        
        // Effet de pulsation si énergie basse
        if (energyPercent < 0.3) {
            double pulse = 0.7 + 0.3 * Math.sin(gameTime * 10);
            gc.setStroke(Color.web("#ff4444", pulse));
            gc.setLineWidth(2);
            gc.strokeRoundRect(10, 10, 250, 35, 8, 8);
        }
    }
    
    private void renderSpecialAbilities() {
        // God Mode
        gc.setFill(godMode ? Color.YELLOW : Color.GRAY);
        gc.fillText("🌟 God Mode (G) - Coût: 100", 10, 230);
        if (godMode) {
            gc.fillText(String.format("⏱️ Temps: %.1fs", godModeTime), 10, 245);
        }
        
        // Capacité spéciale
        gc.setFill(specialAbilityCharges > 0 ? Color.ORANGE : Color.GRAY);
        gc.fillText("💫 Explosion (Q) - Charges: " + specialAbilityCharges, 10, 260);
        
        // Vague amie
        gc.setFill(energy >= 60 ? Color.GREEN : Color.GRAY);
        gc.fillText("👥 Vague Amie (F) - Coût: 60", 10, 275);
    }
    
    private void renderInstructions() {
        gc.setFill(Color.web("#ADD8E6", 0.9));
        gc.setFont(Font.font("Arial", 10));
        gc.fillText("💡 1-5: Unités | TAB: Portée | H: Hitboxes | Q: Explosion", 10, 300);
        gc.fillText("💡 G: God Mode | F: Vague Amie | Espace: Pause | R: Restart", 10, 315);
        gc.fillText("💡 Clic: Placer | Double-clic: Placement rapide | Drag: Placement multiple", 10, 330);
        
        gc.fillText("🎮 Heart of the Void - ULTIMATE EDITION", CANVAS_WIDTH/2 - 100, CANVAS_HEIGHT - 10);
    }
    
    private Color interpolateColor(Color start, Color end, double ratio) {
        double r = start.getRed() + (end.getRed() - start.getRed()) * ratio;
        double g = start.getGreen() + (end.getGreen() - start.getGreen()) * ratio;
        double b = start.getBlue() + (end.getBlue() - start.getBlue()) * ratio;
        return Color.color(r, g, b);
    }
    
    private void renderPauseScreen() {
        gc.setFill(Color.web("#000000", 0.8));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.fillText("⏸️ PAUSE", CANVAS_WIDTH/2 - 100, CANVAS_HEIGHT/2 - 20);
        
        gc.setFont(Font.font("Arial", 18));
        gc.fillText("Appuyez sur ESPACE pour reprendre", CANVAS_WIDTH/2 - 150, CANVAS_HEIGHT/2 + 30);
        
        // Statistiques en pause
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 14));
        gc.fillText("Statistiques:", CANVAS_WIDTH/2 - 60, CANVAS_HEIGHT/2 + 70);
        gc.fillText("Vague: " + wave + " | Score: " + score + " | Kills: " + killCount, 
                   CANVAS_WIDTH/2 - 100, CANVAS_HEIGHT/2 + 95);
    }
    
    private void renderGameOverScreen() {
        gc.setFill(Color.web("#000000", 0.9));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 56));
        gc.fillText("💀 GAME OVER", CANVAS_WIDTH/2 - 180, CANVAS_HEIGHT/2 - 50);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        gc.fillText("🏆 Score final: " + score, CANVAS_WIDTH/2 - 80, CANVAS_HEIGHT/2);
        gc.fillText("🌊 Vagues survivées: " + (wave-1), CANVAS_WIDTH/2 - 100, CANVAS_HEIGHT/2 + 30);
        gc.fillText("💀 Ennemis vaincus: " + killCount, CANVAS_WIDTH/2 - 90, CANVAS_HEIGHT/2 + 60);
        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Appuyez sur R pour recommencer", CANVAS_WIDTH/2 - 120, CANVAS_HEIGHT/2 + 100);
    }
    
    private String getUnitName(int type) {
        return switch (type) {
            case 1 -> "🛡️ The Knight";
            case 2 -> "⚫ Void Vessel";
            case 3 -> "🗡️ Hornet";
            case 4 -> "✨ God Void";
            case 5 -> "⚡ Speedster";
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
        threadPool.shutdown();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

// CLASSE PARTICLE SYSTEM POUR LES EFFETS VISUELS
class ParticleSystem {
    List<Particle> particles = new ArrayList<>();
    double x, y;
    Color color;
    double creationTime;
    double duration;
    
    ParticleSystem(double x, double y, int count, Color color, double duration) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.creationTime = System.currentTimeMillis() / 1000.0;
        this.duration = duration;
        
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(
                x, y,
                (random.nextDouble() - 0.5) * 100,
                (random.nextDouble() - 0.5) * 100,
                color,
                duration * random.nextDouble()
            ));
        }
    }
    
    void update(double deltaTime) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update(deltaTime);
            if (p.isDead()) {
                it.remove();
            }
        }
    }
    
    boolean isDead() {
        return particles.isEmpty();
    }
    
    void render(GraphicsContext gc) {
        for (Particle p : particles) {
            p.render(gc);
        }
    }
}

class Particle {
    double x, y, vx, vy;
    Color color;
    double lifetime;
    double age = 0;
    
    Particle(double x, double y, double vx, double vy, Color color, double lifetime) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
        this.lifetime = lifetime;
    }
    
    void update(double deltaTime) {
        age += deltaTime;
        x += vx * deltaTime;
        y += vy * deltaTime;
        vy += 50 * deltaTime; // Gravité
    }
    
    boolean isDead() {
        return age >= lifetime;
    }
    
    void render(GraphicsContext gc) {
        double alpha = 1.0 - (age / lifetime);
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        double size = 2 + (1 - age/lifetime) * 3;
        gc.fillOval(x - size/2, y - size/2, size, size);
    }
}

// CLASSE SPECIAL EFFECT POUR LES EFFETS SPÉCIAUX
class SpecialEffect {
    String type;
    double x, y;
    double duration;
    double age = 0;
    
    SpecialEffect(String type, double x, double y, double duration) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.duration = duration;
    }
    
    void update(double deltaTime) {
        age += deltaTime;
    }
    
    boolean isFinished() {
        return age >= duration;
    }
    
    void render(GraphicsContext gc) {
        double progress = age / duration;
        double alpha = 1.0 - progress;
        
        switch (type) {
            case "EXPLOSION":
                gc.setStroke(Color.web("#FFA500", alpha));
                gc.setLineWidth(3);
                double radius = 100 * progress;
                gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
                break;
                
            case "GOD_MODE":
                gc.setStroke(Color.web("#FFFF00", alpha));
                gc.setLineWidth(4);
                double godRadius = 80 + Math.sin(age * 10) * 20;
                gc.strokeOval(x - godRadius, y - godRadius, godRadius * 2, godRadius * 2);
                break;
                
            case "WAVE_CLEAR":
                gc.setFill(Color.web("#00FF00", alpha));
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
                gc.fillText("VAGUE " + (int)(progress * 100) + "%", x - 100, y);
                break;
                
            case "BOSS_WAVE":
                gc.setFill(Color.web("#FF0000", alpha));
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
                gc.fillText("👑 VAGUE BOSS APPROCHE!", x - 120, y);
                break;
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
    double attackCooldown = 0;
    double speed;
    double lastDamageTime = 0;
    
    GameUnit(double x, double y, boolean isAlly, int health, int damage, double attackSpeed, double range, Color color, String name, double speed) {
        this.x = x;
        this.y = y;
        this.isAlly = isAlly;
        this.health = this.maxHealth = health;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.range = range;
        this.color = color;
        this.name = name;
        this.speed = speed;
    }
    
    void update(double deltaTime) {
        if (attackCooldown > 0) {
            attackCooldown -= deltaTime;
        }
        if (lastDamageTime > 0) {
            lastDamageTime -= deltaTime;
        }
    }
    
    void moveTowards(double targetX, double targetY, double deltaTime) {
        double dx = targetX - x;
        double distance = Math.abs(dx);
        int floorY = 700 - 100;
        
        if (distance > 10) {
            double moveDistance = speed * deltaTime;
            if (dx > 0) {
                x += moveDistance;
            } else if (dx < 0) {
                x -= moveDistance;
            }
            
            y = floorY;
            x = Math.max(15, Math.min(1200-15, x));
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
        lastDamageTime = 0.3; // Flash when damaged
    }
    
    boolean isAlive() {
        return health > 0;
    }
    
    void render(GraphicsContext gc) {
        // Flash when damaged
        if (lastDamageTime > 0) {
            gc.setFill(Color.WHITE);
            gc.fillOval(x-14, y-14, 28, 28);
        }
        
        // Ombre
        gc.setFill(Color.web("#000000", 0.4));
        gc.fillOval(x-10, y+8, 20, 10);
        
        // Aura
        if (isAlly) {
            gc.setFill(Color.web("#6a0dad", 0.4));
        } else {
            gc.setFill(Color.web("#8b0000", 0.4));
        }
        gc.fillOval(x-16, y-16, 32, 32);
        
        // Corps principal avec dégradé
        for (int i = 12; i > 0; i -= 2) {
            double ratio = (double) i / 12;
            Color shade = color.deriveColor(0, 1, 0.7 + ratio * 0.3, 1);
            gc.setFill(shade);
            gc.fillOval(x-i, y-i, i*2, i*2);
        }
        
        // Barre de vie améliorée
        if (health < maxHealth) {
            renderHealthBar(gc);
        }
        
        // Nom
        gc.setFill(isAlly ? Color.WHITE : Color.LIGHTYELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText(name, x-15, y-30);
    }
    
    private void renderHealthBar(GraphicsContext gc) {
        double healthPercent = (double) health / maxHealth;
        
        gc.setFill(Color.web("#2c2c2c"));
        gc.fillRoundRect(x-18, y-45, 36, 8, 4, 4);
        
        Color healthColor = healthPercent > 0.6 ? Color.GREEN : 
                           healthPercent > 0.3 ? Color.YELLOW : Color.RED;
        gc.setFill(healthColor);
        gc.fillRoundRect(x-18, y-45, 36 * healthPercent, 8, 4, 4);
        
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x-18, y-45, 36, 8, 4, 4);
    }
}

class GameBase {
    double x, y;
    int health, maxHealth;
    boolean isPlayerBase;
    double healCooldown = 0;
    double damageFlash = 0;
    
    GameBase(double x, double y, boolean isPlayerBase) {
        this.x = x;
        this.y = y;
        this.isPlayerBase = isPlayerBase;
        this.health = this.maxHealth = isPlayerBase ? 800 : 600;
    }
    
    void update(double deltaTime) {
        if (isPlayerBase && health < maxHealth) {
            healCooldown += deltaTime;
            if (healCooldown >= 1.5) {
                health = Math.min(maxHealth, health + 8);
                healCooldown = 0;
            }
        }
        
        if (damageFlash > 0) {
            damageFlash -= deltaTime;
        }
    }
    
    void takeDamage(int amount) {
        health = Math.max(0, health - amount);
        damageFlash = 0.2;
    }
    
    boolean isAlive() {
        return health > 0;
    }
    
    void render(GraphicsContext gc) {
        // Flash when damaged
        if (damageFlash > 0) {
            gc.setFill(Color.RED);
            gc.fillRect(x-25, y-45, 50, 90);
        }
        
        Color baseColor = isPlayerBase ? Color.web("#4169e1") : Color.web("#8b0000");
        
        // Ombre
        gc.setFill(Color.web("#000000", 0.5));
        gc.fillRect(x-25, y-40, 50, 85);
        
        // Structure principale avec dégradé
        for (int i = 20; i > 0; i -= 5) {
            double ratio = (double) i / 20;
            Color shade = baseColor.deriveColor(0, 1, 0.6 + ratio * 0.4, 1);
            gc.setFill(shade);
            gc.fillRect(x-i, y-40, i*2, 80);
        }
        
        // Détails architecturaux
        gc.setFill(baseColor.deriveColor(0, 1, 0.4, 1));
        for (int i = 0; i < 4; i++) {
            gc.fillRect(x-18, y-35 + i*20, 36, 4);
        }
        
        // Tour
        Color towerColor = isPlayerBase ? Color.PURPLE : Color.ORANGE;
        gc.setFill(towerColor);
        gc.fillPolygon(new double[]{x-12, x, x+12, x}, 
                      new double[]{y-48, y-60, y-48, y-42}, 4);
        
        renderHealthBar(gc);
        
        // Nom de la base
        gc.setFill(isPlayerBase ? Color.LIGHTBLUE : Color.ORANGE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        String baseName = isPlayerBase ? "🏰 BASE ALLIÉE" : "🏴 BASE ENNEMIE";
        gc.fillText(baseName, x-35, y+60);
    }
    
    private void renderHealthBar(GraphicsContext gc) {
        double healthPercent = (double) health / maxHealth;
        
        gc.setFill(Color.web("#2c2c2c"));
        gc.fillRoundRect(x-35, y-65, 70, 12, 6, 6);
        
        Color healthColor = healthPercent > 0.6 ? Color.GREEN : 
                           healthPercent > 0.3 ? Color.YELLOW : Color.RED;
        gc.setFill(healthColor);
        gc.fillRoundRect(x-35, y-65, 70 * healthPercent, 12, 6, 6);
        
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x-35, y-65, 70, 12, 6, 6);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText(health + "/" + maxHealth, x-20, y-56);
    }
}

class GameProjectile {
    double x, y, targetX, targetY;
    double speed = 300;
    int damage;
    Color color;
    double lifetime;
    double age = 0;
    double trailUpdate = 0;
    
    GameProjectile(double startX, double startY, double targetX, double targetY, int damage, Color color, double range, double attackSpeed) {
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.damage = damage;
        this.color = color;
        this.lifetime = range / speed + 1.5;
        this.speed *= (0.8 + attackSpeed * 0.1);
    }
    
    void update(double deltaTime) {
        age += deltaTime;
        trailUpdate += deltaTime;
        
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx*dx + dy*dy);
        
        if (distance > 2) {
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
        return distance < 20;
    }
    
    void render(GraphicsContext gc) {
        // Traînée dynamique
        if (trailUpdate > 0.02) {
            trailUpdate = 0;
        }
        
        // Aura
        gc.setFill(Color.web(color.toString(), 0.6));
        gc.fillOval(x-8, y-8, 16, 16);
        
        // Noyau
        gc.setFill(color);
        gc.fillOval(x-5, y-5, 10, 10);
        
        // Effet de lumière
        gc.setFill(Color.WHITE);
        gc.fillOval(x-2, y-2, 4, 4);
    }
}

class PowerUp {
    enum Type {
        ENERGY(Color.web("#9d4edd")), 
        HEAL(Color.web("#4CAF50")), 
        DAMAGE_BOOST(Color.web("#FF5722")),
        SPEED_BOOST(Color.web("#2196F3")),
        COMBO(Color.web("#FF9800"));
        
        final Color color;
        Type(Color color) {
            this.color = color;
        }
    }
    
    double x, y;
    Type type;
    double lifetime = 12.0;
    double age = 0;
    double bounce = 0;
    double rotation = 0;
    
    PowerUp(double x, double y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
    
    void update(double deltaTime) {
        age += deltaTime;
        bounce += deltaTime * 6;
        rotation += deltaTime * 90;
    }
    
    boolean isExpired() {
        return age >= lifetime;
    }
    
    boolean contains(double pointX, double pointY) {
        double distance = Math.sqrt((x - pointX)*(x - pointX) + (y - pointY)*(y - pointY));
        return distance < 18;
    }
    
    Color getColor() {
        return type.color;
    }
    
    void render(GraphicsContext gc) {
        double bounceOffset = Math.sin(bounce) * 4;
        double pulse = 0.6 + 0.4 * Math.sin(age * 8);
        double scale = 1.0 + 0.2 * Math.sin(age * 6);
        
        // Aura pulsante
        gc.setFill(Color.web(type.color.toString(), 0.3 * pulse));
        gc.fillOval(x-25, y-25 + bounceOffset, 50, 50);
        
        // Corps rotatif
        gc.save();
        gc.translate(x, y + bounceOffset);
        gc.rotate(rotation);
        
        // Forme géométrique selon le type
        gc.setFill(type.color);
        switch (type) {
            case ENERGY:
                gc.fillRect(-10 * scale, -10 * scale, 20 * scale, 20 * scale);
                break;
            case HEAL:
                gc.fillOval(-12 * scale, -12 * scale, 24 * scale, 24 * scale);
                break;
            case DAMAGE_BOOST:
                double[] starX = {0, 4, 12, 6, 8, 0, -8, -6, -12, -4};
                double[] starY = {-12, -4, -4, 0, 8, 4, 8, 0, -4, -4};
                gc.fillPolygon(starX, starY, 10);
                break;
            case SPEED_BOOST:
                gc.fillPolygon(new double[]{-10, 10, -10}, new double[]{-8, 0, 8}, 3);
                break;
            case COMBO:
                gc.fillRect(-8 * scale, -8 * scale, 16 * scale, 16 * scale);
                break;
        }
        
        gc.restore();
        
        // Icône
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        String icon = switch (type) {
            case ENERGY -> "⚡";
            case HEAL -> "❤️";
            case DAMAGE_BOOST -> "💥";
            case SPEED_BOOST -> "⚡";
            case COMBO -> "🔥";
        };
        gc.fillText(icon, x-6, y + 6 + bounceOffset);
        
        // Timer avec warning
        double timeLeft = lifetime - age;
        Color timerColor = timeLeft < 3.0 ? Color.RED : Color.WHITE;
        gc.setFill(timerColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText(String.format("%.1f", timeLeft), x-8, y + 25 + bounceOffset);
        
        // Clignotement d'avertissement
        if (timeLeft < 3.0 && ((int)(age * 10) % 2) == 0) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeOval(x-18, y-18 + bounceOffset, 36, 36);
        }
    }
}