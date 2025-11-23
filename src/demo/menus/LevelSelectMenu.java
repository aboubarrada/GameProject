package demo.menus;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// Menu de sélection de niveau
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
        
        Button level1Btn = createInvisibleButton(1, 500, 60);
        Button level2Btn = createInvisibleButton(2, 500, 60);
        Button level3Btn = createInvisibleButton(3, 500, 60);
        
        StackPane.setAlignment(level1Btn, Pos.CENTER);
        level1Btn.setTranslateY(-160);
        
        StackPane.setAlignment(level2Btn, Pos.CENTER);
        level2Btn.setTranslateY(-30);
        
        StackPane.setAlignment(level3Btn, Pos.CENTER);
        level3Btn.setTranslateY(110);
        
        root.getChildren().addAll(level1Btn, level2Btn, level3Btn);
        
        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("Heart of the Void - Sélection de Niveau");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    private Button createInvisibleButton(int level, int width, int height) {
        Button btn = new Button();
        btn.setPrefSize(width, height);
        btn.setStyle("-fx-background-color: transparent; " +
                    "-fx-border-color: transparent; " +
                    "-fx-text-fill: transparent;");
        
        btn.setOnAction(e -> startLevel(level));
        
        return btn;
    }
    
    private void startLevel(int levelNumber) {
        try {
            Class<?> gameClass = Class.forName("demo.HeartOfTheVoidGame");
            Object game = gameClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method setLevelMethod = gameClass.getMethod("setLevel", int.class);
            setLevelMethod.invoke(game, levelNumber);
            java.lang.reflect.Method startMethod = gameClass.getMethod("start", Stage.class);
            startMethod.invoke(game, stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}