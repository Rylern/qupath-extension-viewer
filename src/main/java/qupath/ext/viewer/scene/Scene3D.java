package qupath.ext.viewer.scene;

import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import qupath.lib.images.servers.ImageServer;

import java.awt.image.BufferedImage;

public class Scene3D {

    private final SubScene subScene;
    private final Group root;

    public Scene3D(
            ObservableDoubleValue sceneWidth,
            ObservableDoubleValue sceneHeight,
            ImageServer<BufferedImage> imageServer,
            ObservableDoubleValue translationSliderValue,
            ObservableDoubleValue xRotationSliderValue,
            ObservableDoubleValue yRotationSliderValue
    ) {
        root = new Group();

        subScene = new SubScene(root, sceneWidth.get(), sceneHeight.get());
        subScene.setFill(new Color(0.5, 0.5, 0.5, 1));
        subScene.widthProperty().bind(sceneWidth);
        subScene.heightProperty().bind(sceneHeight);

        root.getChildren().add(new AmbientLight());
        root.getTransforms().addAll(new SceneTransformations(subScene, 10).getTransforms());

        root.getChildren().add(new Slicer(
                imageServer.getWidth(),
                imageServer.getHeight(),
                imageServer.nZSlices(),
                translationSliderValue,
                xRotationSliderValue,
                yRotationSliderValue
        ));

        setUpObjects(
                imageServer,
                translationSliderValue,
                xRotationSliderValue,
                yRotationSliderValue
        );
        setUpCamera(2 * Math.max(imageServer.getWidth(), imageServer.getHeight()));
    }

    public SubScene getSubScene() {
        return subScene;
    }

    private void setUpObjects(
            ImageServer<BufferedImage> imageServer,
            ObservableDoubleValue translationSliderValue,
            ObservableDoubleValue xRotationSliderValue,
            ObservableDoubleValue yRotationSliderValue
    ) {
        Rectangle slicer = new Slicer(
                imageServer.getWidth(),
                imageServer.getHeight(),
                imageServer.nZSlices(),
                translationSliderValue,
                xRotationSliderValue,
                yRotationSliderValue
        );
        root.getChildren().add(slicer);

        root.getChildren().add(new Volume(slicer, imageServer));
    }

    private void setUpCamera(int distanceFromOrigin) {
        Camera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(10 * distanceFromOrigin);
        camera.translateZProperty().set(-distanceFromOrigin);
        subScene.setCamera(camera);
    }
}
