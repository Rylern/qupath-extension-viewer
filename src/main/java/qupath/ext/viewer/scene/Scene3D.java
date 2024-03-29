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
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Scene3D {

    private final SubScene subScene;
    private final Group root;

    public Scene3D(
            ObservableDoubleValue sceneWidth,
            ObservableDoubleValue sceneHeight,
            int imageWidth,
            int imageHeight,
            int imageDepth,
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
        root.getTransforms().addAll(new SceneTransformations(subScene).getTransforms());
        setUpObjects(imageWidth, imageHeight, imageDepth, translationSliderValue, xRotationSliderValue, yRotationSliderValue);
        setUpCamera();
    }

    public SubScene getSubScene() {
        return subScene;
    }

    private void setUpObjects(
            int width,
            int height,
            int depth,
            ObservableDoubleValue translationSliderValue,
            ObservableDoubleValue xRotationSliderValue,
            ObservableDoubleValue yRotationSliderValue
    ) {
        Rectangle slicer = createSlicer(width, height, depth, translationSliderValue, xRotationSliderValue, yRotationSliderValue);
        root.getChildren().add(slicer);

        Box box = new Box(width, height, depth);
        box.setMaterial(new PhongMaterial(new Color(1, 0,0, .1)));
        //root.getChildren().add(box);

        root.getChildren().add(new Volume(box, slicer));
    }

    private void setUpCamera() {
        Camera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(1000);
        camera.translateZProperty().set(-50);
        subScene.setCamera(camera);
    }

    private static Rectangle createSlicer(
            int width,
            int height,
            int depth,
            ObservableDoubleValue translationSliderValue,
            ObservableDoubleValue xRotationSliderValue,
            ObservableDoubleValue yRotationSliderValue
    ) {
        Rectangle slicer = new Rectangle(-width, -height, 2*width, 2*height);
        slicer.setFill(Color.BLUE);

        updateSlicerTransforms(slicer, translationSliderValue, xRotationSliderValue, yRotationSliderValue, depth);
        translationSliderValue.addListener((p, o, n) -> updateSlicerTransforms(slicer, translationSliderValue, xRotationSliderValue, yRotationSliderValue, depth));
        xRotationSliderValue.addListener((p, o, n) -> updateSlicerTransforms(slicer, translationSliderValue, xRotationSliderValue, yRotationSliderValue, depth));
        yRotationSliderValue.addListener((p, o, n) -> updateSlicerTransforms(slicer, translationSliderValue, xRotationSliderValue, yRotationSliderValue, depth));

        return slicer;
    }

    private static void updateSlicerTransforms(
            Rectangle slicer,
            ObservableDoubleValue translationSliderValue,
            ObservableDoubleValue xRotationSliderValue,
            ObservableDoubleValue yRotationSliderValue,
            double depth
    ) {
        slicer.getTransforms().setAll(
                new Rotate(yRotationSliderValue.get(), Rotate.Y_AXIS),
                new Rotate(xRotationSliderValue.get(), Rotate.X_AXIS),
                new Translate(0, 0, translationSliderValue.get() - depth/2)
        );
    }
}
