package org.proglan.chatbot;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatbotService {

    private static final String URL_CHAT_API = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "input_api_key_here";
    private static final String USER_ROLE = "user";

    public String chatGPT(String message) {
        try {
            URL obj = new URL(URL_CHAT_API);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            setupConnection(connection);
            sendRequest(connection, message);

            String responseBody = readResponse(connection);
            return extractContentFromResponse(responseBody);
        } catch (IOException e) {
            handleConnectionError();
            throw new RuntimeException(e);
        }
    }

    private void setupConnection(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
    }

    private void sendRequest(HttpURLConnection connection, String message) throws IOException {
        String model = "gpt-3.5-turbo";
        String body = String.format("{\"model\": \"%s\", \"messages\": [{\"role\": \"%s\", \"content\": \"%s\"}]}",
                model, USER_ROLE, message);
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(body);
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    private String extractContentFromResponse(String response) {
        int startMarker = response.indexOf("content") + 11;
        int endMarker = response.indexOf("\"", startMarker);
        return response.substring(startMarker, endMarker);
    }

    private void handleConnectionError() {
        System.err.println("An error occurred in connection or server request.");

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred in connection or server request.");
            alert.showAndWait();
        });
    }
}
