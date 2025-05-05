module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;

    opens com.example to javafx.fxml;
    exports com.example;
}
