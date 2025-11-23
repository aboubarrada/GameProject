package demo.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

// Représente un projectile lancé par une unité
public class GameProjectile {
    public double x, y, targetX, targetY;
    public double speed = 250;
    public int damage;
    public Color color;
    public int projectileType;
    public double lifetime = 4.0;
    public double age = 0;
    
    public GameProjectile(double startX, double startY, double targetX, double targetY, int damage, Color color, int projectileType) {
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.damage = damage;
        this.color = color;
        this.projectileType = projectileType;
    }
    
    public void update(double deltaTime) {
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
    
    public boolean isExpired() {
        return age >= lifetime;
    }
    
    public boolean collidesWith(GameUnit unit) {
        double distance = Math.sqrt((x - unit.x)*(x - unit.x) + (y - unit.y)*(y - unit.y));
        return distance < 18;
    }
    
    public void render(GraphicsContext gc, Image[] projectileImages) {
        if (projectileImages != null && projectileType < projectileImages.length && projectileImages[projectileType] != null) {
            gc.drawImage(projectileImages[projectileType], x-8, y-8, 16, 16);
        } else {
            renderFallbackProjectile(gc);
        }
    }
    
    private void renderFallbackProjectile(GraphicsContext gc) {
        gc.setStroke(Color.web(color.toString(), 0.3));
        gc.setLineWidth(3);
        gc.strokeLine(x-5, y, x+5, y);
        
        gc.setFill(color);
        gc.fillOval(x-4, y-4, 8, 8);
        
        gc.setFill(Color.web(color.toString(), 0.5));
        gc.fillOval(x-6, y-6, 12, 12);
    }
}