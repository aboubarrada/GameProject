package demo.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameUnit {
    public double x, y;
    public int health, maxHealth, damage;
    public double attackSpeed, range;
    public Color color;
    public String name;
    public boolean isAlly;
    public int unitType;
    public double attackCooldown = 0;
    public double speed = 30;
    
    public GameUnit(double x, double y, boolean isAlly, int health, int damage, double attackSpeed, double range, Color color, String name, int unitType) {
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
    
    public void update(double deltaTime) {
        if (attackCooldown > 0) {
            attackCooldown -= deltaTime;
        }
    }
    
    public void moveTowards(double targetX, double targetY, double deltaTime) {
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
    
    public double distanceTo(double otherX, double otherY) {
        double dx = x - otherX;
        double dy = y - otherY;
        return Math.sqrt(dx*dx + dy*dy);
    }
    
    public boolean canAttack() {
        return attackCooldown <= 0;
    }
    
    public void resetAttackCooldown() {
        attackCooldown = 1.0 / attackSpeed;
    }
    
    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }
    
    public boolean isAlive() {
        return health > 0;
    }
    
    public void render(GraphicsContext gc, Image[] allyImages, Image[] enemyImages) {
        gc.setFill(Color.web("#000000", 0.2));
        gc.fillOval(x-6, y+8, 12, 6);
        
        Image unitImage = null;
        if (isAlly && allyImages != null && unitType < allyImages.length && allyImages[unitType] != null) {
            unitImage = allyImages[unitType];
        } else if (!isAlly && enemyImages != null && unitType < enemyImages.length && enemyImages[unitType] != null) {
            unitImage = enemyImages[unitType];
        }
        
        if (unitImage != null) {
            gc.drawImage(unitImage, x-20, y-20, 40, 40);
        } else {
            renderFallbackSprite(gc);
        }
        
        if (health < maxHealth) {
            renderHealthBar(gc);
        }
        
        if (canAttack() && range > 0 && shouldShowRange()) {
            gc.setStroke(Color.web(color.toString(), 0.15));
            gc.setLineWidth(1);
            gc.strokeOval(x-range, y-range, range*2, range*2);
        }
    }
    
    private boolean shouldShowRange() {
        return true;
    }
    
    private void renderFallbackSprite(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x-12, y-12, 24, 24);
        
        gc.setStroke(isAlly ? Color.WHITE : Color.DARKRED);
        gc.setLineWidth(1);
        gc.strokeOval(x-12, y-12, 24, 24);
    }
    
    private void renderHealthBar(GraphicsContext gc) {
        gc.setFill(Color.web("#2c2c2c"));
        gc.fillRoundRect(x-15, y-25, 30, 6, 3, 3);
        
        double healthPercent = (double) health / maxHealth;
        Color healthColor = healthPercent > 0.6 ? Color.GREEN : 
                           healthPercent > 0.3 ? Color.YELLOW : Color.RED;
        gc.setFill(healthColor);
        gc.fillRoundRect(x-15, y-25, 30 * healthPercent, 6, 3, 3);
    }
}