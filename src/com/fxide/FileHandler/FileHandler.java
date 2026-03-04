package com.fxide.FileHandler;

import com.fxide.*;
import com.fxide.NewFile.NewFileBox;
import com.fxide.NewFile.NewFileBoxController;
import com.fxide.NewProject.NewProjectBox;
import com.fxide.ProjectMetadata.ProjectMeta;

import javafx.scene.control.*;
import org.fxmisc.richtext.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;


//TODO: Have this run on a different thread
public class FileHandler {

    private static TreeView<File> fileTree = null;
    private static Controller controller = null;
    private final File openRecentFile = new File("src/com/fxide/IDEmetadata/openRecent.txt");

    public static File openedDirectory = null;
    public static File newFileDirectory = null;
    public static File newProjectDirectory = null;
    public static String newProjectName = null;
    public static Path currentProjectStructureJsonPath = null;

    public static ArrayList<Pair<Tab, File>> tabs = new ArrayList<>();
    public static ArrayList<File> unsavedFiles = new ArrayList<>();


    public FileHandler(TreeView<File> fileTree, Controller controller) {
        //CellFactory
        fileTree.setCellFactory(_ -> {
            TreeCell<File> cell = new TreeCell<>(){
                //Allows for updating the text of the item
                @Override
                protected void updateItem(File item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            };
            //Double click mouse event
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) { //Double click
                    TreeItem<File> item = cell.getTreeItem();
                    try {
                        openFile(cell.getTreeItem().getValue());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            return cell;
        });

        this.fileTree = fileTree;
        this.controller = controller;
    }

    public static void reloadTree() throws IOException {
        loadRootFolder(openedDirectory);
    }

    public static File openFileExplorer(String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);

        File selectedDirectory = directoryChooser.showDialog(new Stage());

        if (selectedDirectory != null) {
            System.out.println("Selected directory: " + selectedDirectory.getAbsolutePath());
        }

        return selectedDirectory;
    }

    public void openProjectWithExplorer(String title) throws IOException {
        File directory = openFileExplorer(title);

        if (directory == null) { //User cancelled
            return;
        }

        //Check if it is a valid - has .data
        boolean isValid = checkForData(directory);
        if (!isValid) {
            Status status = new Status(controller);
            status.setStatusLabelText("Invalid project", 10000);
            return;
        }

        loadRootFolder(directory);
        addToRecentFile(directory);
        addAllToOpenRecentMenu();
    }

    //Verifies the existence of the .data folder in the project, indicating if it is compatible
    public static boolean checkForData(File directory) {
        Path dataDir = directory.toPath().resolve(".data");

        //It exists
        if (Files.exists(dataDir) && Files.isDirectory(dataDir)) {
            System.out.println("Found .data");
            return true;
        } else {
            System.out.println("Missing .data");
            return false;
        }
    }

    public void openFolder(File directory) throws IOException {
        if (directory == null) {
            return;
        }

        loadRootFolder(directory);
        addToRecentFile(directory);
        addAllToOpenRecentMenu();

        openedDirectory = directory;
    }

    public void openFile(File file) throws IOException {
        if (file.isDirectory()) return; //Cannot load in a directory so we exit

        //Setting up the reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader bfro = new BufferedReader(new FileReader(file));
        String st;

        /*
        //Creating the tab
        Label title = new Label(file.getName());
        Button closeButton = new Button("x");
        HBox header = new HBox(title, closeButton);

        Tab tab = new Tab();
        tab.setGraphic(header);

        CodeArea codeArea = new CodeArea();
        codeArea.setWrapText(true);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        tab.setContent(codeArea);
        controller.tabPane.getTabs().add(tab);

        closeButton.setOnAction(e -> {
            try {
                saveFile(file, codeArea);
                controller.tabPane.getTabs().remove(tab);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

         */

        // Create tab with file name
        Tab tab = new Tab(file.getName());
        tab.setClosable(true);

        // Create editor area
        CodeArea codeArea = new CodeArea();
        codeArea.setWrapText(true);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        tab.setContent(codeArea);

        AtomicBoolean saveFailed = new AtomicBoolean(false);

        // Handle close (save before closing)
        tab.setOnCloseRequest(event -> {
            try {
                saveFile(file, codeArea);
            } catch (IOException e) {
                event.consume(); // prevent closing if save fails
                e.printStackTrace();
                saveFailed.set(true);
            }

            if (!saveFailed.get()) { //Saved successfully so we can close
                controller.tabPane.getTabs().remove(tab);
            }
        });

        controller.tabPane.getTabs().add(tab);
        controller.tabPane.getSelectionModel().select(tab);

        // Optional: show close button on all tabs
        controller.tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);


        Pair<Tab, File> pair = new Pair<>(tab, file);
        tabs.add(pair);

        //Adding the string to the rich text area
        while ((st = bfro.readLine()) != null) {
            codeArea.appendText(st);
            codeArea.appendText("\n");
        }

        unsavedFiles.add(file);
    }

    public void addToRecentFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(openRecentFile.toPath());

        //Already in the file so do not add
        if (lines.contains(String.valueOf(file))) return;

        //Contains more than 5 lines so delete the first one
        if (lines.size() >= 5) {
            lines.remove(0);

            //Rewrite the file so that the line is removed
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(openRecentFile))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(openRecentFile, true))) {
            bw.write(String.valueOf(file));
            bw.newLine();
            System.out.println("Success");
        } catch (IOException e) {
            System.out.println("Error writing file: " + openRecentFile);
        }
    }

    //Calls the recursive ui
    public static void loadRootFolder(File directory) throws IOException {
        saveAllFiles(controller);
        controller.closeAllTabs();

        TreeItem<File> rootNode = buildTreeForDirectory(directory);
        fileTree.setRoot(rootNode);
        rootNode.setExpanded(true);
    }

    //Recursive function to generate a file structure
    public static TreeItem<File> buildTreeForDirectory(File directory) {
        TreeItem<File> directoryTreeItem = new TreeItem<>(directory);
        File[] filesList =  directory.listFiles();

        if (filesList == null) {
            return directoryTreeItem;
        }

        for (File file : filesList) {
            if (file.isDirectory()) {
                TreeItem<File> childNode = buildTreeForDirectory(file);
                directoryTreeItem.getChildren().add(childNode);
            }
            else {
                directoryTreeItem.getChildren().add(new TreeItem<>(file));
            }
        }

        return directoryTreeItem;
    }

    public void addToOpenRecentMenu(File file) {
        MenuItem menuItem = new MenuItem(file.getName());
        controller.openRecentMenu.getItems().add(menuItem);

        //Add an event
        menuItem.setOnAction(_ -> {
            try {
                openFolder(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void addAllToOpenRecentMenu() throws IOException {
        controller.openRecentMenu.getItems().clear();

        List<String> lines = Files.readAllLines(openRecentFile.toPath());

        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            addToOpenRecentMenu(new File(line));
        }
    }

    public static Pair<Tab, File> getFocussedTabPair() {
        Tab current = controller.tabPane.getSelectionModel().getSelectedItem();
        Pair<Tab, File> pair = null;

        for (Pair<Tab, File> p: tabs) {
            if (p.key().equals(current)) {
                pair = p;
                break;
            }
        }

        return pair;
    }

    public static void onSaveFileMenu(Controller controller) throws IOException {
        Status status = new Status(controller);
        Pair<Tab, File> tabPair = getFocussedTabPair();
        CodeArea codeArea = (CodeArea) tabPair.key().getContent();
        saveFile(tabPair.value(), codeArea);
        status.setStatusLabelText("Saved file: " + tabPair.value().getName(), 5000);

        unsavedFiles.remove(tabPair.value());
    }

    public static void saveFile(File file, CodeArea codeArea) throws IOException {
        String st = codeArea.getText();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(st);
        }
    }

    public static void saveAllFiles(Controller controller) throws IOException {
        Status status = new Status(controller);
        List<File> saved = new ArrayList<>();

        for (File file : unsavedFiles) {
            for (Pair<Tab, File> pair : tabs) {
                if (pair.value().equals(file)) {
                    CodeArea codeArea = (CodeArea) pair.key().getContent();
                    saveFile(file, codeArea);
                    saved.add(pair.value());
                }
            }
        }

        unsavedFiles.removeAll(saved);
        status.setStatusLabelText("Saved all files", 5000);
    }

    public static void newFileDialogBox() throws IOException {
        NewFileBox fileBox = new NewFileBox();
        fileBox.show();
    }

    public static void newProjectDialogBox() throws IOException {
        NewProjectBox projectBox = new NewProjectBox();
        projectBox.show();
    }

    public static void createFile(File directory, String fileName) throws IOException {
        try {
            File file = new File(directory, fileName);
            if (file.createNewFile()) {
                System.out.println("Success");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void newFile() throws IOException {
        NewFileBoxController newFileBoxController = new NewFileBoxController();
        String fileName = newFileBoxController.newFileName;
    }

    //Deletes the out, src and data folders if they exist and are empty
    public static void deleteOutSrcData (File out, File src, File data) throws IOException {
        Files.deleteIfExists(src.toPath());
        Files.deleteIfExists(out.toPath());
        Files.deleteIfExists(data.toPath());
    }

    //Initializes the data folder when creating a new project
    public static void initData (String projectName, File dataDir) throws IOException {
        ObjectMapper mapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
        Path file = Path.of(dataDir.getAbsolutePath() + File.separator + "project.json"); //Path of the file
        currentProjectStructureJsonPath = file;

        //Assigning the values
        ProjectMeta meta = new ProjectMeta();
        meta.name = projectName;
        meta.type = "java";

        ProjectMeta.ProjectSettings settings = new ProjectMeta.ProjectSettings();
        settings.JDKPath = "";
        settings.JDKVersion = "";
        meta.settings = settings;

        mapper.writeValue(file.toFile(), meta); //Writing to the json
    }

    //Creates the folder structure of the new project
    public static void createNewProject(File directory, String projectName) throws IOException {
        try {
            //Create project directory
            String directoryPath = directory.getAbsolutePath() + File.separator + projectName;
            File newDirectory = new File(directoryPath);
            boolean success;
            if (!newDirectory.exists()) {
                success = newDirectory.mkdir();
            } else {
                System.out.println("Directory already exists");
                return;
            }

            if (success) {
                //Creating out, src and data dir
                File out = new File(directoryPath + File.separator + "out");
                File src = new File(directoryPath + File.separator + "src");
                File data = new File(directoryPath + File.separator + ".data");

                boolean outSuccess, scrSuccess, dataSuccess;

                outSuccess = out.mkdir();
                scrSuccess = src.mkdir();
                dataSuccess = data.mkdir();

                //There has been an issue create at least one of the directories
                if (!outSuccess) {
                    System.out.println("Unable to create directory 'out'");
                    deleteOutSrcData(out, src, data);
                    return;
                } else if (!scrSuccess) {
                    System.out.println("Unable to create directory 'scr'");
                    deleteOutSrcData(out, src, data);
                    return;
                } else if (!dataSuccess) {
                    System.out.println("Unable to create directory 'data'");
                    deleteOutSrcData(out, src, data);
                    return;
                }

                initData(projectName, data);

                Status status = new Status(controller);
                status.setStatusLabelText("New project created", 5000);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}