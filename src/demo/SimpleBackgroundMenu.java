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
import demo.menus.LevelSelectMenu;

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
            System.out.println("✅ Menu background chargé avec succès!");
        } catch (Exception e) {
            System.out.println("Erreur chargement image: " + e.getMessage());
            root.setStyle("-fx-background-color: black;");
        }
        
        // Create title
        Label title = new Label("Heart of the Void");
        title.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 3, 0.5, 0, 2);");
        
        // Create play button
        Button playButton = new Button("Jouer");
        playButton.setPrefSize(200, 60);
        playButton.setStyle("-fx-font-size: 24px; " +
                          "-fx-background-color: rgba(10,10,30,0.85); " +
                          "-fx-text-fill: white; " +
                          "-fx-border-color: rgba(0,255,234,0.8); " +
                          "-fx-border-width: 2px; " +
                          "-fx-background-radius: 8px; " +
                          "-fx-border-radius: 8px; " +
                          "-fx-font-weight: bold;");
        
        playButton.setOnMouseEntered(e -> playButton.setStyle("-fx-font-size: 24px; " +
                          "-fx-background-color: rgba(0,255,234,0.4); " +
                          "-fx-text-fill: white; " +
                          "-fx-border-color: rgba(0,255,234,1.0); " +
                          "-fx-border-width: 2px; " +
                          "-fx-background-radius: 8px; " +
                          "-fx-border-radius: 8px; " +
                          "-fx-font-weight: bold;"));
        
        playButton.setOnMouseExited(e -> playButton.setStyle("-fx-font-size: 24px; " +
                          "-fx-background-color: rgba(10,10,30,0.85); " +
                          "-fx-text-fill: white; " +
                          "-fx-border-color: rgba(0,255,234,0.8); " +
                          "-fx-border-width: 2px; " +
                          "-fx-background-radius: 8px; " +
                          "-fx-border-radius: 8px; " +
                          "-fx-font-weight: bold;"));
        
        playButton.setOnAction(e -> showLevelSelect(stage));
        
        // Arrange UI elements
        VBox menuContainer = new VBox(40);
        menuContainer.setAlignment(Pos.CENTER);
        menuContainer.getChildren().addAll(title, playButton);
        
        root.getChildren().add(menuContainer);
        
        Scene scene = new Scene(root, 1000, 600);
        
        stage.setTitle("Heart of the Void");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
    
    private void showLevelSelect(Stage stage) {
        System.out.println("🎮 Passage à la sélection de niveau");
        
        LevelSelectMenu levelSelect = new LevelSelectMenu();
        levelSelect.start(stage);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}