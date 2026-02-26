package com.CodeEditor;

import com.CodeEditor.FileHandler.FileHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/codeEditor.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        FileHandler fh = new FileHandler(controller.fileTree, controller);

        Scene scene = new Scene(root ,1280, 720);

        stage.setTitle("Sample Application");
        stage.setScene(scene);
        stage.show();

        fh.addAllToOpenRecentMenu();
    }

    static void main(String[] args) {
        launch(args);
    }
}