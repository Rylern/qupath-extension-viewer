package qupath.ext.viewer;

import javafx.fxml.FXML;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.LightBase;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.awt.event.KeyEvent;
import java.io.IOException;

public class Viewer extends Stage {

    @FXML
    private Scene scene;
    @FXML
    private Group root;

    public Viewer(Stage owner) throws IOException {
        initUI(owner);
        populate();
    }

    private void initUI(Stage owner) throws IOException {
        UiUtilities.loadFXML(this, Viewer.class.getResource("viewer.fxml"));

        if (owner != null) {
            initOwner(owner);
        }
        show();
    }

    private void populate() {
        Group group = new Group();
        SubScene subScene = new SubScene(group, 800, 600);
        root.getChildren().add(subScene);

        Box testBox = new Box(1, 1, 1);
        testBox.setMaterial(new PhongMaterial(Color.RED));
        group.getChildren().add(testBox);

        group.getChildren().add(new AmbientLight());

        qupath.ext.viewer.Camera camera = new qupath.ext.viewer.Camera();
        subScene.setCamera(camera);
        subScene.setOnScroll(e -> camera.translate(e.getDeltaY() > 0));

        subScene.setOnKeyPressed(e -> {
            double step = 100;
            switch (e.getCode()) {
                case LEFT:
                    camera.dragY(-step);
                    break;
                case RIGHT:
                    camera.dragY(step);
                    break;
                case UP:
                    camera.dragX(-step);
                    break;
                case DOWN:
                    camera.dragX(step);
                    break;
                default:
                    break;
            }
        });
    }
}
