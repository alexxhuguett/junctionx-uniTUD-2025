package org.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage stage) {
        Button btn = new Button("Hello from JavaFX Client!");
        btn.setOnAction(e -> System.out.println("Clicked!"));

        stage.setScene(new Scene(new StackPane(btn), 400, 200));
        stage.setTitle("JunctionX Desktop Client");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
