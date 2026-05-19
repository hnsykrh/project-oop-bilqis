package com.hnsykrh.blooddonation.fx;

import com.hnsykrh.blooddonation.service.ServiceException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class FxUtil {

    public static final String[] BLOOD_TYPES = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    private FxUtil() {
    }

    public static void showError(Window owner, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR, message);
            if (owner != null) {
                alert.initOwner(owner);
            }
            alert.setTitle("Error");
            alert.showAndWait();
        });
    }

    public static void showInfo(Window owner, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION, message);
            if (owner != null) {
                alert.initOwner(owner);
            }
            alert.setTitle("Information");
            alert.showAndWait();
        });
    }

    public static boolean confirm(Window owner, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION, message);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle("Confirm");
        return alert.showAndWait().filter(button -> button.getButtonData().isDefaultButton()).isPresent();
    }

    public static <T> void runAsync(Window owner, Callable<T> task, Consumer<T> onSuccess) {
        Task<T> javafxTask = new Task<>() {
            @Override
            protected T call() throws Exception {
                return task.call();
            }
        };
        javafxTask.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(javafxTask.getValue())));
        javafxTask.setOnFailed(e -> {
            Throwable cause = javafxTask.getException();
            String msg = cause instanceof ServiceException se ? se.getMessage() : cause.getMessage();
            showError(owner, msg != null ? msg : "Operation failed.");
        });
        Thread thread = new Thread(javafxTask);
        thread.setDaemon(true);
        thread.start();
    }
}
