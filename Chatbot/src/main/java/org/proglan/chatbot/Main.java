package org.proglan.chatbot;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent splashRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("splash-screen.fxml")));
            Parent rootMain = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("chat-ui.fxml")));

            StackPane root = new StackPane(rootMain, splashRoot);

            Scene scene = new Scene(root, 400, 600);
            primaryStage.setTitle("ChatBot");
            primaryStage.setScene(scene);

            primaryStage.setMinWidth(300);
            primaryStage.setMinHeight(400);

            primaryStage.show();

            PauseTransition delay = getPauseTransition(splashRoot, root);
            delay.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PauseTransition getPauseTransition(Parent splashRoot, StackPane root) {
        Duration splashDuration = Duration.seconds(2);

        FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1), splashRoot);
        fadeSplash.setFromValue(1.0);
        fadeSplash.setToValue(0.0);
        fadeSplash.setOnFinished(event -> root.getChildren().remove(splashRoot));

        PauseTransition delay = new PauseTransition(splashDuration);
        delay.setOnFinished(event -> fadeSplash.play());
        return delay;
    }
}
