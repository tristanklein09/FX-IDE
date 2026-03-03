package com.CodeEditor.Compiler;

import javafx.application.Platform;

import javax.tools.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.CodeEditor.Controller.outputTextArea;
import static com.CodeEditor.Controller.problemsTextArea;
import static com.CodeEditor.FileHandler.FileHandler.openedDirectory;

//TODO: Check if there is bug that will make it so that the classes in out are still ran even if there is a compilation error
public class Compiler {

    public Path outPath = openedDirectory.toPath().resolve("out");
    public Path srcPath = openedDirectory.toPath().resolve("src");

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
                problemsTextArea.append(String.valueOf(kind), "error");
                problemsTextArea.append(String.valueOf(line), "error");
                problemsTextArea.append(message, "error");
                problemsTextArea.append(String.valueOf(file), "error");
            });

        }

        return  success;
    }

    public void run() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", outPath.toString(), "Main");
        processBuilder.directory(new File(openedDirectory.toURI()));
        Process process = processBuilder.start();

        System.out.println("Running...");

        //Stream output
        stream(process, process.getErrorStream(), "error"); //errors
        stream(process, process.getInputStream(), "info"); //output

        //Wait for process to finish
        new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                Platform.runLater(() ->
                        outputTextArea.append("Process exited with code " + exitCode + "\n", "info")
                );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();


    }

    //Stream the output line by line to the output and problems tabs
    private void stream(Process process, InputStream stream, String style) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> outputTextArea.append(finalLine + "\n", style));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
