package com.CodeEditor.Compiler;

import com.CodeEditor.Controller;
import javafx.application.Platform;

import javax.tools.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

//import static com.CodeEditor.Controller.consoleTextArea;
//import static com.CodeEditor.Controller.problemsTextArea;
import static com.CodeEditor.FileHandler.FileHandler.openedDirectory;
import static com.CodeEditor.Controller.isRunning;

//TODO: Check if there is bug that will make it so that the classes in out are still ran even if there is a compilation error
public class Compiler {

    private Process currentProcess;
    private BufferedWriter processInputWriter;
    private Controller controller;

    public Path outPath = openedDirectory.toPath().resolve("out");
    public Path srcPath = openedDirectory.toPath().resolve("src");

    //private Process currentProcess;
    //private BufferedWriter processInputWriter;

    public Compiler(Controller controller) {
        this.controller = controller;
    }

    public boolean compile() throws IOException {
        System.out.println("Compiling...");

        if (!Files.exists(srcPath)) {
            throw new RuntimeException("src folder not found at: " + srcPath.toAbsolutePath());
        }

        //Getting the compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("No Java compiler found, please use a JDK");
        }

        //Capturing errors to put in the 'problems panel'
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        //File manager
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);

        //Get source files
        List<File> sourceFiles = Files.walk(srcPath)
                .filter(p -> p.toString().endsWith(".java"))
                .map(Path::toFile)
                .collect(Collectors.toList());

        //Convert to JavaFileObjects
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles);

        List<String> options = List.of("-d", outPath.toAbsolutePath().toString());

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        boolean success = task.call();
        fileManager.close();

        System.out.println("Compilation success: " + success);
        System.out.println("JDK Compiler: " + ToolProvider.getSystemJavaCompiler());
        System.out.println("Found " + sourceFiles.size() + " source file(s)");

        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            Diagnostic.Kind kind = diagnostic.getKind();
            long line =  diagnostic.getLineNumber();
            String message = diagnostic.getMessage(null);
            Path file = Paths.get(diagnostic.getSource().toUri());

            //Send to problems panel
            Platform.runLater(() -> { //Run on javafx application thread
                Controller.problemsTextArea.append(String.valueOf(kind), "error");
                Controller.problemsTextArea.append(String.valueOf(line), "error");
                Controller.problemsTextArea.append(message, "error");
                Controller.problemsTextArea.append(String.valueOf(file), "error");
            });

        }

        return  success;
    }

    public void run() throws IOException {
        isRunning = true;
        System.out.println("isRunning: " + isRunning);

        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", outPath.toString(), "Main");
        processBuilder.directory(new File(openedDirectory.toURI()));

        currentProcess = processBuilder.start();
        processInputWriter = new BufferedWriter(
                new OutputStreamWriter(currentProcess.getOutputStream()));

        System.out.println("Running...");

        currentProcess = processBuilder.start();
        processInputWriter = new BufferedWriter(
                new OutputStreamWriter(currentProcess.getOutputStream()));

        //Stream output
        stream(currentProcess.getErrorStream(), "error");
        stream(currentProcess.getInputStream(), "info");

        //Wait for process to finish
        new Thread(() -> {
            try {
                int exitCode = currentProcess.waitFor();
                controller.appendToConsole("Process exited with code " + exitCode + "\n", "info");

                isRunning = false; //Processes finished here
                System.out.println("isRunning: " + isRunning);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();



    }

    //Stream the output line by line to the output and problems tabs
    private void stream(InputStream stream, String style) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    controller.appendToConsole(finalLine + "\n", style);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendInput(String input) throws IOException {
        if (processInputWriter != null) {
            processInputWriter.write(input);
            processInputWriter.newLine();
            processInputWriter.flush();
        }
    }

}