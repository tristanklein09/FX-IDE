package com.fxide.ProjectStructure;

import com.fxide.ProjectMetadata.ProjectMeta;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static com.fxide.FileHandler.FileHandler.openedDirectory;

public class ProjectStructureBoxController implements Initializable {

    @FXML
    private Button cancelButton;
    @FXML
    private Button okButton;
    @FXML
    private Button applyButton;
    @FXML
    public TextField JDKPathText;
    @FXML
    public TextField JDKVersionText;

    public static String JDKPath;
    public static String JDKVersion;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        getProjectStructure();

        JDKPathText.setText(JDKPath);
        JDKVersionText.setText(JDKVersion);

        cancelButton.setOnAction(_ -> stage.close());

        applyButton.setOnAction(_ -> applyProjectStructure());

        okButton.setOnAction(_ -> {
            applyProjectStructure();
            stage.close();
        });
    }

    private void applyProjectStructure() {
        ObjectMapper mapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
        Path file = Path.of(openedDirectory + File.separator + ".data" + File.separator + "project.json"); //path of the json project file

        //TODO: Get it to read the current .json file and only change the settings part that actually changed
        ProjectMeta meta = new ProjectMeta();
        ObjectMapper reader = new ObjectMapper();

        try {
            ProjectMeta metaReader = reader.readValue(file.toFile(), ProjectMeta.class);
            meta.name = metaReader.name;
            meta.type = metaReader.type;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ProjectMeta.ProjectSettings settings = new ProjectMeta.ProjectSettings();
        settings.JDKPath = JDKPathText.getText();
        settings.JDKVersion = JDKVersionText.getText();
        meta.settings = settings;

        mapper.writeValue(file.toFile(), meta);
    }

    private void getProjectStructure() {
        ObjectMapper mapper = new ObjectMapper();
        Path file = Path.of(openedDirectory + File.separator + ".data" + File.separator + "project.json");

        try {
            ProjectMeta meta = mapper.readValue(file.toFile(), ProjectMeta.class);
            JDKPath = meta.settings.JDKPath;
            JDKVersion = meta.settings.JDKVersion;
        } catch (Exception e) {
            throw new RuntimeException("Error reading project structure json file: " + e);
        }
    }
}