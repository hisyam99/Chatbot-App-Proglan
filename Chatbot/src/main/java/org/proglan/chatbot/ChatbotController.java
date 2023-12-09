package org.proglan.chatbot;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.HttpURLConnection;

public class ChatbotController {

    private static final String SEND_BUTTON_SEND = "Send";
    private static final String SEND_BUTTON_CANCEL = "Cancel";
    public BorderPane topSection;
    public StackPane centerSection;
    public ProgressIndicator cancelProgressIndicator;
    public VBox bottomSection;
    public Label textLabel;
    public ImageView themeIcon;
    public ImageView menuIcon;
    public ImageView aboutIcon;
    public ScrollPane scrollPane;
    public BorderPane mainBorderPane;

    @FXML
    private TextArea chatArea;
    @FXML
    private TextField userInput;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Button sendButton;
    @FXML
    private ToggleButton themeSwitch;
    @FXML
    private VBox navigationDrawer;

    private ChatbotService chatbotService;
    private HttpURLConnection connection;
    private boolean isDrawerOpen = true;

    public void initialize() {
        chatbotService = new ChatbotService();
        toggleDrawer();
        navigationDrawer.setVisible(false);
        configureUserInput();
        toggleTheme();
    }

    private void configureUserInput() {
        userInput.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                sendMessage();
                event.consume();
            }
        });
    }

    public void sendMessage() {
        String userMessage = userInput.getText().trim();
        if (!userMessage.isEmpty()) {
            toggleUserInput(true);

            Task<String> task = createChatTask(userMessage);
            task.setOnRunning(event -> toggleProgressIndicator(true));
            task.setOnSucceeded(event -> handleTaskCompletion(userMessage, task.getValue()));

            new Thread(task).start();
            userInput.clear();
        }
    }

    private Task<String> createChatTask(String userMessage) {
        return new Task<>() {
            @Override
            protected String call() {
                return chatbotService.chatGPT(userMessage);
            }
        };
    }

    private void toggleUserInput(boolean disable) {
        userInput.setDisable(disable);
        setSendButtonState(disable);
    }

    private void setSendButtonState(boolean isSending) {
        userInput.setDisable(isSending);
        sendButton.setText(isSending ? SEND_BUTTON_CANCEL : SEND_BUTTON_SEND);
        sendButton.setOnAction(isSending ? e -> cancelRequest() : e -> sendMessage());
        sendButton.setStyle(isSending ? "-fx-background-color: #e74c3c; -fx-text-fill: white;" : "-fx-background-color: #3498db; -fx-text-fill: white;");
    }

    private void handleTaskCompletion(String userMessage, String response) {
        appendToChat("You", userMessage);
        appendToChat("Chatbot", response);
        toggleProgressIndicator(false);
        toggleUserInput(false);
    }

    private void appendToChat(String sender, String message) {
        chatArea.appendText(sender + ": " + message + "\n");
    }

    private void toggleProgressIndicator(boolean show) {
        progressIndicator.setVisible(show);
        progressIndicator.setManaged(show);
    }

    public void cancelRequest() {
        if (progressIndicator.isVisible()) {
            progressIndicator.setVisible(false);
            progressIndicator.setManaged(false);

            if (connection != null) {
                connection.disconnect();
                toggleProgressIndicator(false);
            }

            userInput.setDisable(false);
            setSendButtonState(false);
        }
    }

    public void toggleTheme() {
        if (themeSwitch.isSelected()) {
            setDarkMode();
        } else {
            setLightMode();
        }
    }

    private void setDarkMode() {
        themeIcon.setImage(new Image(getClass().getResourceAsStream("images/icons/dark_theme_icon.png")));
        menuIcon.setImage(new Image(getClass().getResourceAsStream("images/icons/dark_menu_icon.png")));
        aboutIcon.setImage(new Image(getClass().getResourceAsStream("images/icons/dark_about_icon.png")));
        chatArea.setStyle("-fx-control-inner-background:#455a64; -fx-text-fill: white;");
        userInput.setStyle("-fx-background-color: #546e7a; -fx-text-fill: white;");
        topSection.setStyle("-fx-background-color: #263238; -fx-text-fill: white;");
        textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold");
        bottomSection.setStyle("-fx-background-color: #263238;");
        scrollPane.setStyle("-fx-background-color: #455a64; -fx-background: none; -fx-border-style: none;");
        navigationDrawer.setStyle("-fx-background-color: #607d8b;");
        mainBorderPane.setStyle("-fx-background-color: #607d8b;");
    }

    private void setLightMode() {
        themeIcon.setImage(new Image(getClass().getResourceAsStream("images/icons/theme_icon.png")));
        menuIcon.setImage(new Image(getClass().getResourceAsStream("images/icons/menu_icon.png")));
        aboutIcon.setImage(new Image(getClass().getResourceAsStream("images/icons/about_icon.png")));
        chatArea.setStyle("-fx-control-inner-background: #f5f5f5; -fx-text-fill: #212121;");
        userInput.setStyle("-fx-background-color: #eeeeee; -fx-text-fill: #212121;");
        topSection.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: black;");
        textLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold");
        bottomSection.setStyle("-fx-background-color: #FFFFFF;");
        scrollPane.setStyle("-fx-background-color: #f5f5f5; -fx-background: none; -fx-border-style: none;");
        navigationDrawer.setStyle("-fx-background-color: #eeeeee;");
        mainBorderPane.setStyle("-fx-background-color: #eeeeee;");
    }

    public void showAboutInfo() {
        String aboutInfo = "Muhammad Hisyam Kamil\t\t\t(202210370311060)\nAhmad Naufal Luthfan Marzuqi\t\t(202210370311072)\nFarriel Arrianta Akbar Pratama\t\t(202210370311077)\n\nPemrograman Lanjut 3D\n\nMade with love.";

        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About");
        aboutAlert.setHeaderText(null);
        aboutAlert.setContentText(aboutInfo);
        aboutAlert.showAndWait();
    }

    public void toggleDrawer() {
        navigationDrawer.setVisible(true);
        Timeline timeline = new Timeline();
        TranslateTransition transition = new TranslateTransition(Duration.millis(350), navigationDrawer);

        if (isDrawerOpen) {
            KeyValue widthValue = new KeyValue(navigationDrawer.prefWidthProperty(), 0);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(350), widthValue);
            timeline.getKeyFrames().add(keyFrame);
            transition.setToX(-navigationDrawer.getWidth());
        } else {
            KeyValue widthValue = new KeyValue(navigationDrawer.prefWidthProperty(), 200);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(350), widthValue);
            timeline.getKeyFrames().add(keyFrame);
            transition.setToX(0);
        }

        timeline.play();
        transition.play();
        isDrawerOpen = !isDrawerOpen;
    }

    public void setConnection(HttpURLConnection connection) {
        this.connection = connection;
    }
}
