package demo.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class UIRenderer {
    private static final int CANVAS_WIDTH = 1000;
    private static final int CANVAS_HEIGHT = 600;
    private static final int MAX_ENERGY = 200;
    
    private Image moneyImage;
    private Image playerBaseImage;
    private Image enemyBaseImage;
    
    public UIRenderer() {
        try {
            moneyImage = new Image("file:resources/images/ui/Money.png");
        } catch (Exception e) {
            System.out.println("Impossible de charger l'icône monnaie: " + e.getMessage());
        }
        try {
            playerBaseImage = new Image("file:resources/images/ui/Void_statue.png");
        } catch (Exception e) {
            playerBaseImage = null;
        }
        try {
            enemyBaseImage = new Image("file:resources/images/ui/radiance_statue.png");
        } catch (Exception e) {
            enemyBaseImage = null;
        }
    }
    
    public void renderGameUI(GraphicsContext gc, int energy, int selectedUnitType, int[] unitCosts, Image[] unitImages,
                             int playerBaseHealth, int playerBaseMax, int enemyBaseHealth, int enemyBaseMax) {
        renderEnergyBar(gc, energy);
        renderUnitCards(gc, energy, selectedUnitType, unitCosts, unitImages);
        
        renderControlsHint(gc);
    }
    
    private void renderEnergyBar(GraphicsContext gc, int energy) {
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
            gc.fillText("Money: " + energy + "/" + MAX_ENERGY, 15, 30);
        }
    }
    
    private void renderUnitCards(GraphicsContext gc, int energy, int selectedUnitType, int[] unitCosts, Image[] unitImages) {
        int cardCount = unitCosts.length;
        int cardW = 90;
        int cardH = 90;
        int spacing = 16;
        int totalW = cardCount * cardW + (cardCount - 1) * spacing;
        int startX = (CANVAS_WIDTH - totalW) / 2;
        int y = 10;

        for (int i = 0; i < cardCount; i++) {
            int x = startX + i * (cardW + spacing);
            gc.setFill(Color.web("#111111", 0.6));
            gc.fillRoundRect(x, y, cardW, cardH, 8, 8);

            Image img = (unitImages != null && i < unitImages.length) ? unitImages[i] : null;
            if (img != null) {
                gc.drawImage(img, x + 10, y + 10, cardW - 20, cardH - 38);
            } else {
                gc.setFill(Color.DARKGRAY);
                gc.fillRoundRect(x + 10, y + 10, cardW - 20, cardH - 38, 6, 6);
            }

            gc.setFill(Color.GOLD);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            String costText = "" + unitCosts[i];
            gc.fillText(costText, x + cardW/2 - 8, y + cardH - 10);

            if (selectedUnitType == i+1) {
                gc.setStroke(Color.web("#00ffea", 0.9));
                gc.setLineWidth(3);
                gc.strokeRoundRect(x, y, cardW, cardH, 8, 8);
            }

            if (energy < unitCosts[i]) {
                gc.setFill(Color.web("#000000", 0.45));
                gc.fillRoundRect(x, y, cardW, cardH, 8, 8);
            }
        }
    }

    private void renderBaseIcons(GraphicsContext gc, int playerBaseHealth, int playerBaseMax, int enemyBaseHealth, int enemyBaseMax) {
        // left: player base icon
        int iconSize = 48;
        int padding = 8;
        if (playerBaseImage != null) {
            gc.drawImage(playerBaseImage, padding, padding, iconSize, iconSize);
        }
        // health bar
        double pct = (double)playerBaseHealth / Math.max(1, playerBaseMax);
        gc.setFill(Color.web("#2c2c2c"));
        gc.fillRoundRect(padding + iconSize + 6, padding + 6, 120, 12, 4, 4);
        gc.setFill(Color.GREEN);
        gc.fillRoundRect(padding + iconSize + 6, padding + 6, 120 * pct, 12, 4, 4);

        // right: enemy base icon
        int rx = CANVAS_WIDTH - padding - iconSize;
        if (enemyBaseImage != null) {
            gc.drawImage(enemyBaseImage, rx, padding, iconSize, iconSize);
        }
        double epct = (double)enemyBaseHealth / Math.max(1, enemyBaseMax);
        gc.setFill(Color.web("#2c2c2c"));
        gc.fillRoundRect(rx - 126, padding + 6, 120, 12, 4, 4);
        gc.setFill(Color.RED);
        gc.fillRoundRect(rx - 126, padding + 6, 120 * epct, 12, 4, 4);
    }
    
    private void renderControls(GraphicsContext gc) {
        gc.setFill(Color.web("#ADD8E6", 0.8));
        gc.setFont(Font.font("Arial", 10));
        gc.fillText("Touches 1-4: Sélection | Espace: Pause | R: Restart", 10, 175);
        gc.fillText("Zone de placement des alliés - Cliquez pour placer!", 100, CANVAS_HEIGHT - 10);
    }
    
    private void renderControlsHint(GraphicsContext gc) {
        gc.setFill(Color.web("#000000", 0.6));
        gc.fillRoundRect(CANVAS_WIDTH - 280, CANVAS_HEIGHT - 50, 270, 40, 6, 6);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("1-4: Unités | Espace: Pause | R: Reset", CANVAS_WIDTH - 275, CANVAS_HEIGHT - 30);
        gc.fillText("Échap: Retour Menu", CANVAS_WIDTH - 275, CANVAS_HEIGHT - 15);
    }
    
    public void renderPauseScreen(GraphicsContext gc) {
        gc.setFill(Color.web("#000000", 0.7));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gc.fillText("PAUSE", CANVAS_WIDTH/2 - 80, CANVAS_HEIGHT/2);
        
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Appuyez sur ESPACE pour reprendre", CANVAS_WIDTH/2 - 120, CANVAS_HEIGHT/2 + 40);
    }
    
    public void renderGameOverScreen(GraphicsContext gc, int score, int wave) {
        gc.setFill(Color.web("#000000", 0.8));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.fillText("GAME OVER", CANVAS_WIDTH/2 - 150, CANVAS_HEIGHT/2);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Score final: " + score + " | Vagues: " + (wave-1), CANVAS_WIDTH/2 - 100, CANVAS_HEIGHT/2 + 40);
        gc.fillText("Appuyez sur R pour recommencer", CANVAS_WIDTH/2 - 90, CANVAS_HEIGHT/2 + 60);
    }
    
    private String getUnitName(int type) {
        return switch (type) {
            case 1 -> "The Knight";
            case 2 -> "Void Vessel";
            case 3 -> "Hornet";
            case 4 -> "God Void";
            default -> "Unknown";
        };
    }
}