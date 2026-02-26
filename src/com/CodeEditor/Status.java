package com.CodeEditor;

import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;


//Controls the status label text
public class Status {

    private final Controller controller;

    public Status(Controller controller) {
        this.controller = controller;
    }

    //Defining a task
    class ResetText extends TimerTask {
        @Override
        public void run() {
            Platform.runLater(() -> controller.statusLabel.setText(""));
        }
    }

    //WaitTime is in milliseconds
    public void setStatusLabelText(String text, long waitTime) {
        Timer timer = new Timer();
        TimerTask task = new ResetText();

        Platform.runLater(() -> controller.statusLabel.setText(text));

        //Waits before resetting
        timer.schedule(task, waitTime);
    }

}
