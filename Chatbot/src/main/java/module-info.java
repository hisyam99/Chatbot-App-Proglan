module org.proglan.chatbot.chatbot {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.proglan.chatbot to javafx.fxml;
    exports org.proglan.chatbot;
}