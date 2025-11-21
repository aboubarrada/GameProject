package demo.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameBase {
    public double x, y;
    public int health, maxHealth;
    public boolean isPlayerBase;
    private Image baseImage;
    
    public GameBase(double x, double y, boolean isPlayerBase) {
        this.x = x;
        this.y = y;
        this.isPlayerBase = isPlayerBase;
        this.health = this.maxHealth = isPlayerBase ? 500 : 300;
        
        // Load the appropriate base image
        loadBaseImage();
    }
    
    private void loadBaseImage() {
        try {
            if (isPlayerBase) {
                baseImage = new Image("file:resources/images/ui/Void_statue.png");
            } else {
                baseImage = new Image("file:resources/images/ui/radiance_statue.png");
            }
        } catch (Exception e) {
            baseImage = null;
            System.out.println("Erreur chargement image base: " + e.getMessage());
        }
    }
    
    public void update(double deltaTime) {
        // Supprimer la régénération automatique de la base du joueur
        // Les bases ne se régénèrent plus automatiquement
    }
    
    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }
    
    public boolean isAlive() {
        return health > 0;
    }
    
    public void render(GraphicsContext gc) {
        // Force les bases à être au sol (sécurité)
        if (y < 400) { // Si la base est trop haute
            y = 500; // Force au sol (CANVAS_HEIGHT - 100)
            System.out.println("⚠️  Base repositionnée au sol: " + (isPlayerBase ? "Player" : "Enemy"));
        }
        
        // Use the statue images instead of colored rectangles
        if (baseImage != null) {
            double imageSize = 80;
            gc.drawImage(baseImage, x - imageSize/2, y - imageSize, imageSize, imageSize);
        } else {
            // Fallback to simple colored rectangle if image fails to load
            renderFallbackBase(gc);
        }
        
        // Health bar above the base
        renderHealthBar(gc);
    }
    
    private void renderFallbackBase(GraphicsContext gc) {
        Color baseColor = isPlayerBase ? Color.web("#4169e1") : Color.web("#8b0000");
        
        // Ombre
        gc.setFill(Color.web("#000000", 0.4));
        gc.fillRect(x-22, y-38, 44, 88);
        
        // Corps principal de la base
        gc.setFill(baseColor);
        gc.fillRect(x-20, y-40, 40, 80);
        
        // Détails architecturaux
        renderBaseDetails(gc, baseColor);
        
        // Drapeau/Toit
        renderFlag(gc);
    }
    
    private void renderBaseDetails(GraphicsContext gc, Color baseColor) {
        gc.setFill(Color.web(baseColor.toString()).darker());
        gc.fillRect(x-18, y-35, 36, 5);
        gc.fillRect(x-18, y-15, 36, 5);
        gc.fillRect(x-18, y+5, 36, 5);
        gc.fillRect(x-18, y+25, 36, 5);
    }
    
    private void renderFlag(GraphicsContext gc) {
        if (isPlayerBase) {
            gc.setFill(Color.PURPLE);
        } else {
            gc.setFill(Color.ORANGE);
        }
        gc.fillPolygon(new double[]{x-10, x, x+10, x}, 
                      new double[]{y-45, y-55, y-45, y-40}, 4);
    }
    
    private void renderHealthBar(GraphicsContext gc) {
        gc.setFill(Color.web("#2c2c2c"));
        gc.fillRoundRect(x-30, y-90, 60, 8, 4, 4);
        
        double healthPercent = (double) health / maxHealth;
        Color healthColor = healthPercent > 0.6 ? Color.GREEN : 
                           healthPercent > 0.3 ? Color.YELLOW : Color.RED;
        gc.setFill(healthColor);
        gc.fillRoundRect(x-30, y-90, 60 * healthPercent, 8, 4, 4);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText(health + "/" + maxHealth, x-20, y-95);
    }
}