package demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import demo.HeartOfTheVoidGame;

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
            System.out.println("❌ Erreur chargement image: " + e.getMessage());
            root.setStyle("-fx-background-color: black;");
        }
        
        javafx.scene.control.Button playButton = new javafx.scene.control.Button();
        playButton.setPrefSize(200, 50);
        playButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        playButton.setOnAction(e -> startGame(stage));
        
        StackPane.setAlignment(playButton, javafx.geometry.Pos.CENTER);
        playButton.setTranslateY(70);
        
        playButton.setOnMouseEntered(e -> playButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-border-color: transparent;"));
        playButton.setOnMouseExited(e -> playButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"));
        
        root.getChildren().add(playButton);
        
        Scene scene = new Scene(root, 1000, 600);
        
        stage.setTitle("Heart of the Void - Menu Background");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
    
    private void startGame(Stage stage) {
        System.out.println("🎮 Bouton JOUER cliqué !");
        
        HeartOfTheVoidGame game = new HeartOfTheVoidGame();
        game.start(stage);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}