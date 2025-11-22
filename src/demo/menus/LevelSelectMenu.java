package demo.menus;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class LevelSelectMenu extends Application {
    
    private Stage stage;
    
    public void start(Stage stage) {
        this.stage = stage;
        showLevelSelection();
    }
    
    private void showLevelSelection() {
        StackPane root = new StackPane();
        
        try {
            Image backgroundImage = new Image("file:resources/images/backgrounds/Level Background.png");
            ImageView background = new ImageView(backgroundImage);
            background.setFitWidth(1000);
            background.setFitHeight(600);
            background.setPreserveRatio(false);
            root.getChildren().add(background);
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #0a0a0a;");
        }
        
        // Boutons invisibles positionnés sur les textes du background
        Button level1Btn = createInvisibleButton(1);
        Button level2Btn = createInvisibleButton(2);
        Button level3Btn = createInvisibleButton(3);
        
        // POSITIONS VRAIMENT DIFFÉRENTES - X ET Y DIFFÉRENTS POUR CHAQUE BOUTON
        // City of Tears - EN HAUT À GAUCHE
        level1Btn.setLayoutX(150);  // À GAUCHE
        level1Btn.setLayoutY(100);  // EN HAUT
        System.out.println("Bouton 1 - City of Tears créé à (150, 100)");
        
        // Radiance Arena - AU CENTRE
        level2Btn.setLayoutX(400);  // AU CENTRE
        level2Btn.setLayoutY(300);  // AU MILIEU
        System.out.println("Bouton 2 - Radiance Arena créé à (400, 300)");
        
        // Nightmare Realm - EN BAS À DROITE
        level3Btn.setLayoutX(650);  // À DROITE
        level3Btn.setLayoutY(500);  // EN BAS
        System.out.println("Bouton 3 - Nightmare Realm créé à (650, 500)");
        
        root.getChildren().addAll(level1Btn, level2Btn, level3Btn);
        System.out.println("Tous les 3 boutons ajoutés au root");
        
        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("Heart of the Void - Sélection de Niveau");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    private Button createInvisibleButton(int level) {
        Button btn = new Button();
        btn.setPrefSize(200, 80); // Plus grands pour être sûrs de les voir
        btn.setStyle("-fx-background-color: transparent; " +
                    "-fx-border-color: transparent; " +
                    "-fx-text-fill: transparent;");
        
        // Couleur différente selon le niveau pour les distinguer
        String hoverColor;
        if (level == 1) {
            hoverColor = "rgba(255, 0, 0, 0.7)"; // Rouge pour niveau 1
        } else if (level == 2) {
            hoverColor = "rgba(0, 255, 0, 0.7)"; // Vert pour niveau 2
        } else {
            hoverColor = "rgba(0, 0, 255, 0.7)"; // Bleu pour niveau 3
        }
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle("-fx-background-color: " + hoverColor + "; " +
                        "-fx-border-color: transparent; " +
                        "-fx-text-fill: transparent;");
            System.out.println("Survol bouton niveau " + level);
        });
        
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; " +
                    "-fx-border-color: transparent; " +
                    "-fx-text-fill: transparent;"));
        
        btn.setOnAction(e -> {
            System.out.println("Clic sur niveau " + level);
            startLevel(level);
        });
        
        return btn;
    }
    
    private void startLevel(int levelNumber) {
        System.out.println("Lancement du niveau " + levelNumber);
        
        try {
            // Utiliser la réflexion pour éviter la dépendance circulaire
            Class<?> gameClass = Class.forName("demo.HeartOfTheVoidGame");
            Object game = gameClass.getDeclaredConstructor().newInstance();
            
            // Appeler setLevel
            java.lang.reflect.Method setLevelMethod = gameClass.getMethod("setLevel", int.class);
            setLevelMethod.invoke(game, levelNumber);
            
            // Appeler start
            java.lang.reflect.Method startMethod = gameClass.getMethod("start", Stage.class);
            startMethod.invoke(game, stage);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du lancement du niveau: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}