package qupath.ext.viewer.scene;

import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.ListChangeListener;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
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
        subScene.widthProperty().bind(sceneWidth);
        subScene.heightProperty().bind(sceneHeight);

        root.getChildren().add(new AmbientLight());
        setUpObjects(imageWidth, imageHeight, imageDepth, translationSliderValue, xRotationSliderValue, yRotationSliderValue);
        root.getTransforms().addAll(new SceneTransformations(subScene).getTransforms());
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
        Rectangle slicer = setUpSlicer(width, height, depth, translationSliderValue, xRotationSliderValue, yRotationSliderValue);

        Box box = new Box(width, height, depth);
        box.setMaterial(new PhongMaterial(new Color(1, 0,0, .1)));
        root.getChildren().add(box);

        MeshView meshView = new MeshView(IntersectionCalculator.getIntersectionMeshBetweenBoxAndRectangle(box, slicer));
        meshView.setMaterial(new PhongMaterial(Color.GREEN));
        slicer.getTransforms().addListener((ListChangeListener<? super Transform>) change ->
                meshView.setMesh(IntersectionCalculator.getIntersectionMeshBetweenBoxAndRectangle(box, slicer))
        );
        root.getChildren().add(meshView);
    }

    private void setUpCamera() {
        Camera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(1000);
        camera.translateZProperty().set(-50);
        subScene.setCamera(camera);
    }

    private Rectangle setUpSlicer(
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

        root.getChildren().add(slicer);

        return slicer;
    }

    private void updateSlicerTransforms(
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
