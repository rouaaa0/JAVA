package com.example.pi;

import com.gluonhq.attach.storage.StorageService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {


    @Override
    public void start(Stage stage) throws Exception {

        URL fxmlLocation = getClass().getResource("/Fxml/Calendar.fxml");
        System.out.println("FXML path: " + fxmlLocation);
        Parent parent = FXMLLoader.load(fxmlLocation);
        Scene scene = new Scene(parent);
        stage.setTitle("Crud");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        // Réduire les logs des packages Gluon
        Logger.getLogger("com.gluonhq.attach").setLevel(Level.SEVERE);
        Logger.getLogger("com.gluonhq.impl.maps").setLevel(Level.SEVERE);
        Logger.getLogger("com.gluonhq.attach.util").setLevel(Level.SEVERE);

        // Facultatif : retirer les anciens handlers pour ne rien afficher
        Logger rootLogger = Logger.getLogger("");
        for (var handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.SEVERE);
        }

        // Lancer l'application JavaFX
        launch(args);

    }
}
