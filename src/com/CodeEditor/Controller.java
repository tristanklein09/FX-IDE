package com.CodeEditor;

import com.CodeEditor.FileHandler.FileHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.fxmisc.richtext.CodeArea;

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
        openMenuItem.setOnAction(_ -> {
            try {
                openFolder();
            } catch (IOException e) {
                throw new RuntimeException("Open menu item action error: " + e);
            }
        });

        saveMenuItem.setOnAction(_ -> {
           try {
               onSaveFileMenu(this);

           } catch (Exception e) {
               throw new RuntimeException("Save menu item action error: " + e);
           }
        });

        saveAllMenuItem.setOnAction(_ -> {
            try {
                saveAllFiles(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        newMenuItem.setOnAction(_ -> {
            try {
                File directory = openFileExplorer("Select folder");
                newFileDirectory = directory;
                newFileDialogBox();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void openFolder() throws IOException {
        FileHandler fileHandler = new FileHandler(fileTree, this);
        fileHandler.openFolderWithExplorer("Select Folder");
    }
}