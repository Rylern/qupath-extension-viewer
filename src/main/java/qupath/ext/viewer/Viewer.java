package qupath.ext.viewer;

import javafx.fxml.FXML;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.stage.Stage;

import java.io.IOException;

public class Viewer extends Stage {

    @FXML
    private Group root;

    public Viewer(Stage owner) throws IOException {
        initUI(owner);
        populate();
    }

    private void initUI(Stage owner) throws IOException {
        UiUtilities.loadFXML(this, Viewer.class.getResource("viewer.fxml"));

        setWidth(800);
        setHeight(600);

        if (owner != null) {
            initOwner(owner);
        }
        show();
    }

    private void populate() {
        Group volume = new Volume(getScene(), 10, 10, 5);

        SubScene subScene = new SubScene(volume, getWidth(), getHeight());
        subScene.widthProperty().bind(getScene().widthProperty());
        subScene.heightProperty().bind(getScene().heightProperty());
        root.getChildren().add(subScene);

        Camera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(1000);
        camera.translateZProperty().set(-50);
        subScene.setCamera(camera);
    }
}
