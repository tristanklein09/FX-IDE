package com.CodeEditor.ProjectMetadata;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"name", "type", "settings"}) //Forces the order
//Template for the project.json file in .data
public class ProjectMeta {

    public String name;
    public String type;
    public ProjectSettings settings;

    public static class ProjectSettings {
        public String JDKPath;
        public String JDKVersion;
    }
}