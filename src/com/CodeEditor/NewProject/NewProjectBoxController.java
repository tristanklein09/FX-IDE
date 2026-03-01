package com.CodeEditor.NewProject;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.CodeEditor.FileHandler.FileHandler.*;

public class NewProjectBoxController implements Initializable {

    @FXML
    private Button newProjectCancelButton;

    @FXML
    private Button newProjectCreateButton;

    @FXML
    private TextField newProjectTextField;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        newProjectCancelButton.setOnAction(_ -> stage.close());

        newProjectCreateButton.setOnAction(_ -> {
            newProjectName = newProjectTextField.getText();
            stage.close();

            try {
                createNewProject(newProjectDirectory, newProjectName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}