package demo;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SimpleBackgroundMenu extends Application {
    
    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        
        try {
            Image backgroundImage = new Image("file:resources/images/backgrounds/Menu_Background.png");
            ImageView background = new ImageView(backgroundImage);
            background.setFitWidth(1000);
            background.setFitHeight(600);
            background.setPreserveRatio(false);
            root.getChildren().add(background);
            System.out.println("Menu background chargé avec succès!");
        } catch (Exception e) {
            System.out.println("Erreur chargement image: " + e.getMessage());
            root.setStyle("-fx-background-color: black;");
        }
        
        // Bouton invisible pour démarrer le jeu - ajustez la position selon votre background
        Button invisiblePlayButton = new Button();
        invisiblePlayButton.setPrefSize(250, 80); // Taille réduite
        invisiblePlayButton.setStyle("-fx-background-color: transparent; " +
                                   "-fx-border-color: transparent; " +
                                   "-fx-text-fill: transparent;");
        
        // Effet grisé au survol
        invisiblePlayButton.setOnMouseEntered(e -> invisiblePlayButton.setStyle("-fx-background-color: rgba(128, 128, 128, 0.6); " +
                                   "-fx-border-color: transparent; " +
                                   "-fx-text-fill: transparent;"));
        
        invisiblePlayButton.setOnMouseExited(e -> invisiblePlayButton.setStyle("-fx-background-color: transparent; " +
                                   "-fx-border-color: transparent; " +
                                   "-fx-text-fill: transparent;"));
        
        invisiblePlayButton.setOnAction(e -> showLevelSelect(stage));
        
        // Positionner le bouton sur la zone "Jouer" du background
        invisiblePlayButton.setLayoutX(375); // Centré horizontalement pour la zone "Jouer"
        invisiblePlayButton.setLayoutY(500); // Sur la zone "Jouer" du background
        
        root.getChildren().add(invisiblePlayButton);
        
        Scene scene = new Scene(root, 1000, 600);
        
        stage.setTitle("Heart of the Void");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
    
    private void showLevelSelect(Stage stage) {
        System.out.println("Passage à la sélection de niveau");
        
        try {
            Class<?> levelSelectClass = Class.forName("demo.menus.LevelSelectMenu");
            Object levelSelect = levelSelectClass.getDeclaredConstructor().newInstance();
            
            java.lang.reflect.Method startMethod = levelSelectClass.getMethod("start", Stage.class);
            startMethod.invoke(levelSelect, stage);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du passage au menu de sélection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}