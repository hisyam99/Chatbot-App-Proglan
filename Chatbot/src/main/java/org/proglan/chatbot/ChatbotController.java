package org.proglan.chatbot;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatbotController {

    private static final String URL_CHAT_API = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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";
    private static final String USER_ROLE = "user";
    private static final String SEND_BUTTON_SEND = "Send";
    private static final String SEND_BUTTON_CANCEL = "Cancel";
    private static final String DARK_MODE_STYLE = "-fx-control-inner-background:#455a64; -fx-text-fill: white;";
    private static final String LIGHT_MODE_STYLE = "-fx-control-inner-background: #FFFFFF; -fx-text-fill: black;";
    public ProgressIndicator cancelProgressIndicator;
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
    private HttpURLConnection connection;

    public void initialize() {
        userInput.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                sendMessage();
                event.consume();
            }
        });
        toggleTheme();
    }

    public void sendMessage() {
        String userMessage = userInput.getText().trim();
        if (!userMessage.isEmpty()) {
            userInput.setDisable(true);
            setSendButtonState(true);

            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    return chatGPT(userMessage);
                }
            };

            task.setOnRunning(event -> showLoading(true));
            task.setOnSucceeded(event -> handleTaskCompletion(userMessage, task.getValue()));

            new Thread(task).start();
            userInput.clear();
        }
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
        showLoading(false);
        userInput.setDisable(false);
        setSendButtonState(false);
    }

    private String chatGPT(String message) {
        try {
            URL obj = new URL(URL_CHAT_API);
            connection = (HttpURLConnection) obj.openConnection();
            setupConnection();
            sendRequest(message);

            String responseBody = readResponse();
            return extractContentFromResponse(responseBody);
        } catch (IOException e) {
            handleConnectionError();
            throw new RuntimeException(e);
        }
    }

    private void setupConnection() throws IOException {
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + ChatbotController.API_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
    }

    private void sendRequest(String message) throws IOException {
        String model = "gpt-3.5-turbo";
        String body = String.format("{\"model\": \"%s\", \"messages\": [{\"role\": \"%s\", \"content\": \"%s\"}]}",
                model, USER_ROLE, message);
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(body);
        }
    }

    private String readResponse() throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    private void handleConnectionError() {
        if (connection != null) {
            connection.disconnect();
            showLoading(false);
        }
    }

    private String extractContentFromResponse(String response) {
        int startMarker = response.indexOf("content") + 11;
        int endMarker = response.indexOf("\"", startMarker);
        return response.substring(startMarker, endMarker);
    }

    private void appendToChat(String sender, String message) {
        chatArea.appendText(sender + ": " + message + "\n");
    }

    private void showLoading(boolean show) {
        progressIndicator.setVisible(show);
        progressIndicator.setManaged(show);
    }

    public void cancelRequest() {
        if (progressIndicator.isVisible()) {
            progressIndicator.setVisible(false);
            progressIndicator.setManaged(false);

            if (connection != null) {
                connection.disconnect();
                showLoading(false);
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
        chatArea.setStyle(DARK_MODE_STYLE);
        userInput.setStyle("-fx-background-color: #546e7a; -fx-text-fill: white;");
        // Penyesuaian tema lainnya untuk mode gelap
    }

    private void setLightMode() {
        chatArea.setStyle(LIGHT_MODE_STYLE);
        userInput.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: black;");
        // Penyesuaian tema lainnya untuk mode terang
    }

    public void showAboutInfo() {
        String aboutInfo = "Muhammad Hisyam Kamil\t\t\t(202210370311060)\nAhmad Naufal Luthfan Marzuqi\t\t(202210370311072)\nFarriel Arrianta Akbar Pratama\t\t(202210370311077)\n\nPemrograman Lanjut 3D\n\nMade with love.";

        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About");
        aboutAlert.setHeaderText(null);
        aboutAlert.setContentText(aboutInfo);
        aboutAlert.showAndWait();
    }
}
