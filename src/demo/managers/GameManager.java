package demo.managers;

import demo.entities.GameUnit;
import demo.entities.GameBase;
import demo.entities.GameProjectile;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.Random;

public class GameManager {
    private static final int CANVAS_WIDTH = 1000;
    private static final int CANVAS_HEIGHT = 600;
    
    private Random random = new Random();
    
    public int getAllyCost(int type) {
        return switch (type) {
            case 1 -> 40;  // Knight: 25 → 40 (+60%)
            case 2 -> 60;  // Vessel: 35 → 60 (+71%) 
            case 3 -> 85;  // Hornet: 45 → 85 (+89%)
            case 4 -> 150; // GodVoid: 65 → 150 (+131%) - TRÈS CHER!
            default -> 40;
        };
    }
    
    public GameUnit createAlly(int type, double x, double y) {
        return switch (type) {
            case 1 -> new GameUnit(x, y, true, 80, 25, 1.5, 60, Color.SILVER, "🛡️Knight", 0);
            case 2 -> new GameUnit(x, y, true, 60, 30, 2.0, 80, Color.PURPLE, "⚫Vessel", 1);
            case 3 -> new GameUnit(x, y, true, 50, 35, 2.5, 120, Color.HOTPINK, "🗡️Hornet", 2);
            case 4 -> new GameUnit(x, y, true, 120, 50, 3.0, 100, Color.GOLD, "✨GodVoid", 3);
            default -> null;
        };
    }
    
    public GameUnit createEnemy(int wave, double difficultyMultiplier) {
        int floorY = CANVAS_HEIGHT - 100;
        return createEnemy(wave, difficultyMultiplier, floorY);
    }
    
    public GameUnit createEnemy(int wave, double difficultyMultiplier, double floorY) {
        int enemyType = determineEnemyType(wave);
        
        int baseHealth = getBaseEnemyHealth(enemyType);
        int baseDamage = getBaseEnemyDamage(enemyType);
        double baseSpeed = getBaseEnemySpeed(enemyType);
        double baseRange = getBaseEnemyRange(enemyType);
        String enemyName = getEnemyName(enemyType);
        Color enemyColor = getEnemyColor(enemyType);
        
        int scaledHealth = (int)(baseHealth * difficultyMultiplier);
        int scaledDamage = (int)(baseDamage * difficultyMultiplier);
        
        return new GameUnit(CANVAS_WIDTH-80, floorY, false, 
            scaledHealth, scaledDamage, baseSpeed, baseRange, enemyColor, enemyName, enemyType - 1);
    }
    
    public int getEnemyReward(int enemyType, int wave) {
        int baseReward = switch (enemyType + 1) {
            case 1 -> 8;
            case 2 -> 12;
            case 3 -> 15;
            case 4 -> 25;
            default -> 8;
        };
        return (int)(baseReward * (1 + wave * 0.1));
    }
    
    public double calculateSpawnInterval(int wave, double difficultyMultiplier) {
        // Intervalles de spawn plus agressifs pour plus de difficulté
        double baseInterval = wave <= 3 ? 2.0 : wave <= 6 ? 1.5 : wave <= 10 ? 1.2 : 0.8; // Plus rapide
        return Math.max(0.6, baseInterval - difficultyMultiplier * 0.3); // Minimum plus bas, réduction plus forte
    }
    
    public GameUnit findClosestTarget(GameUnit unit, List<GameUnit> targets) {
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
    
    public GameProjectile createProjectile(GameUnit unit, GameUnit target, boolean isAlly) {
        int projType = isAlly ? (unit.unitType == 2 ? 1 : 0) : 2;
        return new GameProjectile(
            unit.x, unit.y, target.x, target.y, 
            unit.damage, isAlly ? Color.CYAN : Color.RED, projType
        );
    }
    
    private int determineEnemyType(int wave) {
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
}