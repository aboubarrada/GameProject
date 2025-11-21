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
import demo.HeartOfTheVoidGame;

public class LevelSelectMenu extends Application {
    
    private Stage stage;
    
    public void start(Stage stage) {
        this.stage = stage;
        showLevelSelection();
    }
    
    private void showLevelSelection() {
        StackPane root = new StackPane();
        
        try {
            Image backgroundImage = new Image("file:resources/images/backgrounds/City_of_Tears_background.png");
            ImageView background = new ImageView(backgroundImage);
            background.setFitWidth(1000);
            background.setFitHeight(600);
            background.setPreserveRatio(false);
            root.getChildren().add(background);
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #0a0a0a;");
        }
        
        Button level1Btn = createLevelButton("City of Tears", 1);
        Button level2Btn = createLevelButton("Radiance Arena", 2);
        Button level3Btn = createLevelButton("Nightmare Realm", 3);
        
        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(level1Btn, level2Btn, level3Btn);
        
        root.getChildren().add(buttonContainer);
        
        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("Heart of the Void - Sélection de Niveau");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    private Button createLevelButton(String text, int level) {
        Button btn = new Button(text);
        btn.setPrefSize(280, 50);
        btn.setStyle("-fx-font-size: 16px; " +
                    "-fx-background-color: rgba(10,10,30,0.85); " +
                    "-fx-text-fill: white; " +
                    "-fx-border-color: rgba(0,255,234,0.8); " +
                    "-fx-border-width: 2px; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-border-radius: 6px; " +
                    "-fx-font-weight: bold;");
        
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-font-size: 16px; " +
                    "-fx-background-color: rgba(0,255,234,0.4); " +
                    "-fx-text-fill: white; " +
                    "-fx-border-color: rgba(0,255,234,1.0); " +
                    "-fx-border-width: 2px; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-border-radius: 6px; " +
                    "-fx-font-weight: bold;"));
        
        btn.setOnMouseExited(e -> btn.setStyle("-fx-font-size: 16px; " +
                    "-fx-background-color: rgba(10,10,30,0.85); " +
                    "-fx-text-fill: white; " +
                    "-fx-border-color: rgba(0,255,234,0.8); " +
                    "-fx-border-width: 2px; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-border-radius: 6px; " +
                    "-fx-font-weight: bold;"));
        
        btn.setOnAction(e -> startLevel(level));
        
        return btn;
    }
    
    private void startLevel(int levelNumber) {
        System.out.println("🎮 Lancement du niveau " + levelNumber);
        
        HeartOfTheVoidGame game = new HeartOfTheVoidGame();
        game.setLevel(levelNumber);
        game.start(stage);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}