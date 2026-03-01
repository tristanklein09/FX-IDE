package com.CodeEditor.NewFile;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.CodeEditor.FileHandler.FileHandler.*;

public class NewFileBoxController implements Initializable {

    @FXML
    private Button newFileCancelButton;

    @FXML
    private Button newFileCreateButton;

    @FXML
    private TextField newFileTextField;

    private Stage stage;

    public String newFileName;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        newFileCancelButton.setOnAction(_ -> stage.close());

        newFileCreateButton.setOnAction(_ -> {
            newFileName = newFileTextField.getText();
            stage.close();

            try {
                createFile(newFileDirectory, newFileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            reloadTree();
        });
    }
}