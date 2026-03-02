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

    public void compile() throws IOException {
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
        List<File> sourceFiles = Files.walk(Path.of(openedDirectory + File.separator + "src"))
                .filter(p -> p.toString().endsWith(".java"))
                .map(Path::toFile)
                .collect(Collectors.toList());

        //Convert to JavaFileObjects
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles);

        List<String> options = List.of("-d", "out");

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        boolean success = task.call();

        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            Diagnostic.Kind kind = diagnostic.getKind();
            long line =  diagnostic.getLineNumber();
            String message = diagnostic.getMessage(null);
            Path file = Paths.get(diagnostic.getSource().toUri());

            //Send to problems panel
        }

    }


}
