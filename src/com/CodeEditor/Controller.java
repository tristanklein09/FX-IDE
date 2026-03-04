package com.CodeEditor;

import com.CodeEditor.Compiler.Compiler;
import com.CodeEditor.FileHandler.FileHandler;
import com.CodeEditor.NewProject.NewProjectBoxController;
import com.CodeEditor.ProjectStructure.ProjectStructureBox;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.CodeEditor.FileHandler.FileHandler.*;

//This class is responsible for handling all the events
//It simply detects the events and then calls some other function
public class Controller implements Initializable {

    private int inputStartPosition = 0;
    private Compiler currentCompiler;
    public static boolean isRunning = false;

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
    @FXML
    public MenuItem projectStructureMenuItem;
    @FXML
    public MenuItem runMenuItem;
    @FXML
    public Button consoleButton;
    @FXML
    public Button problemsButton;
    @FXML
    public SplitPane toolWindowSplitPane;
    @FXML
    public SplitPane treeCodeSplitPane;
    @FXML
    public Tab consoleTab;
    @FXML
    public Tab problemsTab;

    private Object outputPS;

    public static StyleClassedTextArea consoleTextArea =  new StyleClassedTextArea();
    public static StyleClassedTextArea problemsTextArea = new StyleClassedTextArea();


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Add genericStyled area to the tool window tabs
        VBox outputWrapper = new VBox(consoleTextArea);
        VBox problemsWrapper = new VBox(problemsTextArea);
        VBox.setVgrow(consoleTextArea, Priority.ALWAYS);
        VBox.setVgrow(problemsTextArea, Priority.ALWAYS);

        consoleTab.setClosable(false);
        consoleTab.setContent(outputWrapper);
        problemsTab.setClosable(false);
        problemsTab.setContent(problemsWrapper);

        consoleTextArea.setEditable(true);
        consoleTextArea.setWrapText(true);
        problemsTextArea.setEditable(true);
        problemsTextArea.setWrapText(true);

        consoleTextArea.getStyleClass().add("com/CodeEditor/resources/css/ToolWindow/outputTextArea.css");
        problemsTextArea.getStyleClass().add("com/CodeEditor/resources/css/ToolWindow/problemsTextArea.css");

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
                closeAllTabs();
                fh.openFolder(new File(newProjectDirectory + File.separator + newProjectName));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //Project Structure
        projectStructureMenuItem.setOnAction(_ -> {
            ProjectStructureBox psb = new ProjectStructureBox();
            try {
                psb.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //Run
        runMenuItem.setOnAction(_ -> {
            //Compile and run
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        currentCompiler = new Compiler(Controller.this);
                        boolean success = currentCompiler.compile();
                        if (success) {
                            currentCompiler.run();
                        } else {
                            System.out.println("Compilation error");
                        }

                        return null;
                    }
                };

                task.setOnFailed(e -> {
                    System.out.println("Task failed");
                    task.getException().printStackTrace();
                });
                new Thread(task).start();
        });

        consoleButton.setOnAction(_ -> {

        });

        problemsButton.setOnAction(_ -> {

        });

        consoleTextArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {

            // Prevent editing previous output
            if (consoleTextArea.getCaretPosition() < inputStartPosition) {
                consoleTextArea.moveTo(consoleTextArea.getLength());
            }

            if (event.getCode() == KeyCode.BACK_SPACE &&
                    consoleTextArea.getCaretPosition() <= inputStartPosition) {
                event.consume();
            }

            if (isRunning) {
                // Handle enter key (send input to running process)
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    event.consume(); // Prevent default newline behavior

                    int caretPosition = consoleTextArea.getCaretPosition();
                    String input = consoleTextArea.getText(inputStartPosition, caretPosition);

                    try {
                        if (currentCompiler != null) {
                            currentCompiler.sendInput(input);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    consoleTextArea.appendText("\n");
                    inputStartPosition = consoleTextArea.getLength();
                }
            }

        });


    }

    public void appendToConsole(String text, String style) {
        Platform.runLater(() -> {
            consoleTextArea.append(text, style);
            inputStartPosition = consoleTextArea.getLength();
        });
    }

    private void openFolder() throws IOException {
        FileHandler fileHandler = new FileHandler(fileTree, this);
        fileHandler.openProjectWithExplorer("Select Folder");
    }

    public void closeAllTabs() {
        tabPane.getTabs().clear();
    };
}