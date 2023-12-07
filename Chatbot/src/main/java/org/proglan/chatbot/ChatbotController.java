package org.proglan.chatbot;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatbotController {

    public ProgressIndicator cancelProgressIndicator;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField userInput;
    @FXML
    private ProgressIndicator progressIndicator;
    private HttpURLConnection connection;

    public void initialize() {
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
            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    return chatGPT(userMessage);
                }
            };

            task.setOnRunning(event -> showLoading(true));
            task.setOnSucceeded(event -> {
                String response = task.getValue();
                appendToChat("You", userMessage);
                appendToChat("Chatbot", response);
                showLoading(false);
            });

            new Thread(task).start();
            userInput.clear();
        }
    }

    private String chatGPT(String message) {
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = "input_api_key_here";

        try {
            URL obj = new URL(url);
            connection = (HttpURLConnection) obj.openConnection();
            setupConnection(apiKey);
            sendRequest(message);

            String responseBody = readResponse();
            return extractContentFromResponse(responseBody);
        } catch (IOException e) {
            handleConnectionError();
            throw new RuntimeException(e);
        }
    }

    private void setupConnection(String apiKey) throws IOException {
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
    }

    private void sendRequest(String message) throws IOException {
        // Simpan model sebagai variabel anggota kelas
        String model = "gpt-3.5-turbo";
        String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}]}";
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
        if (connection != null) {
            connection.disconnect();
            showLoading(false);
        }
    }
}