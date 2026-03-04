package com.fxide.ProjectStructure;


import com.fxide.NewFile.NewFileBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ProjectStructureBox {

    public void show() throws IOException {
        FXMLLoader loader = new FXMLLoader(NewFileBox.class.getResource("/com/fxide/resources/projectStructureBox.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Project Structure");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        ProjectStructureBoxController controller = loader.getController();
        controller.setStage(stage);

        stage.showAndWait();
    }
}

