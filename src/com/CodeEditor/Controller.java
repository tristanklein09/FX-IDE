package com.CodeEditor;

import com.CodeEditor.FileHandler.FileHandler;
import com.CodeEditor.NewProject.NewProjectBoxController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.CodeEditor.FileHandler.FileHandler.*;

//This class is responsible for handling all the events
//It simply detects the events and then calls some other function
public class Controller implements Initializable {

    @FXML
    public MenuItem openMenuItem;
    @FXML
    public TreeView<File> fileTree;
    @FXML
    public TabPane tabPane;
    @FXML
    public Menu openRecentMenu;
    @FXML
    public MenuItem saveMenuItem;
    @FXML
    public Label statusLabel;
    @FXML
    public MenuItem saveAllMenuItem;
    @FXML
    public MenuItem newMenuItem;
    @FXML
    public MenuItem newProjectMenuItem;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Open project
        openMenuItem.setOnAction(_ -> {
            try {
                openFolder();
            } catch (IOException e) {
                throw new RuntimeException("Open menu item action error: " + e);
            }
        });

        //Save
        saveMenuItem.setOnAction(_ -> {
           try {
               onSaveFileMenu(this);

           } catch (Exception e) {
               throw new RuntimeException("Save menu item action error: " + e);
           }
        });

        //Save All
        saveAllMenuItem.setOnAction(_ -> {
            try {
                saveAllFiles(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        //New File
        newMenuItem.setOnAction(_ -> {
            try {
                newFileDirectory = openFileExplorer("Select folder");
                newFileDialogBox();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        //New Project
        newProjectMenuItem.setOnAction(_ -> {
            FileHandler fh = new FileHandler(fileTree, this); //?
            NewProjectBoxController npbc = new NewProjectBoxController();
            try {
                newProjectDirectory = openFileExplorer("Select directory");
                newProjectDialogBox();
                saveAllFiles(this);
                fh.openFolder(new File(newProjectDirectory + File.separator + newProjectName));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void openFolder() throws IOException {
        FileHandler fileHandler = new FileHandler(fileTree, this);
        fileHandler.openProjectWithExplorer("Select Folder");
    }
}