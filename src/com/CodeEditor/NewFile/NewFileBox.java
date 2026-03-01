package com.CodeEditor.NewFile;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class NewFileBox {

    public void show() throws IOException {
        FXMLLoader loader = new FXMLLoader(NewFileBox.class.getResource("/com/CodeEditor/resources/newFileBox.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("New File");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        NewFileBoxController controller = loader.getController();
        controller.setStage(stage);

        stage.showAndWait();
    }
}

