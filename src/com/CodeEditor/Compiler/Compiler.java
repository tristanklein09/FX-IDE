package com.CodeEditor.Compiler;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.CodeEditor.FileHandler.FileHandler.openedDirectory;

public class Compiler {

    public Path outPath = openedDirectory.toPath().resolve("out");
    public Path srcPath = openedDirectory.toPath().resolve("src");

    public void compile() throws IOException {
        //Path srcPath = openedDirectory.toPath().resolve("src");

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

        System.out.println("Compilation success:" + success);
        System.out.println(ToolProvider.getSystemJavaCompiler());
        System.out.println("Found" + sourceFiles.size() + " source files");

        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            Diagnostic.Kind kind = diagnostic.getKind();
            long line =  diagnostic.getLineNumber();
            String message = diagnostic.getMessage(null);
            Path file = Paths.get(diagnostic.getSource().toUri());

            //Send to problems panel
            System.out.println("----");
            System.out.println("Kind: " + diagnostic.getKind());
            System.out.println("File: " + diagnostic.getSource());
            System.out.println("Line: " + diagnostic.getLineNumber());
            System.out.println("Message: " + diagnostic.getMessage(null));
        }

    }

    public void run() throws IOException {
        //Path outPath = openedDirectory.toPath().resolve("out");

        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", outPath.toString(), "Main");
        processBuilder.directory(new File(openedDirectory.toURI()));
        Process process = processBuilder.start();

        System.out.println("Running...");

        //Read output and errors
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String errors = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

        //Send output to output panel and errors to problems panel
        System.out.println(errors);
        System.out.println(output);
    }

}
