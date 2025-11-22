package demo;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        } catch (Exception e) {
            root.setStyle("-fx-background-color: black;");
        }
        
        Button invisiblePlayButton = new Button();
        invisiblePlayButton.setPrefSize(200, 65);
        invisiblePlayButton.setStyle("-fx-background-color: transparent; " +
                                   "-fx-border-color: transparent; " +
                                   "-fx-text-fill: transparent;");
        
        invisiblePlayButton.setOnAction(e -> showLevelSelect(stage));
        
        StackPane.setAlignment(invisiblePlayButton, Pos.CENTER);
        invisiblePlayButton.setTranslateY(75);
        
        root.getChildren().add(invisiblePlayButton);
        
        Scene scene = new Scene(root, 1000, 600);
        
        stage.setTitle("Heart of the Void");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
    
    private void showLevelSelect(Stage stage) {
        try {
            Class<?> levelSelectClass = Class.forName("demo.menus.LevelSelectMenu");
            Object levelSelect = levelSelectClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method startMethod = levelSelectClass.getMethod("start", Stage.class);
            startMethod.invoke(levelSelect, stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}