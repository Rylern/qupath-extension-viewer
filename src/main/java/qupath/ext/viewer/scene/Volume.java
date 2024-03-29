package qupath.ext.viewer.scene;

import javafx.collections.ListChangeListener;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;

class Volume extends Group {

    private final Box box;
    private final Rectangle rectangle;

    public Volume(Box box, Rectangle rectangle) {
        this.box = box;
        this.rectangle = rectangle;

        draw();
        rectangle.getTransforms().addListener((ListChangeListener<? super Transform>) change -> draw());
    }

    private void draw() {
        getChildren().clear();
        for (Mesh mesh: VolumeCalculator.getMeshesOfBoxInFrontOfRectangle(box, rectangle)) {
            MeshView meshView = new MeshView(mesh);
            meshView.setMaterial(new PhongMaterial(Color.GREEN));
            getChildren().add(meshView);
        }
    }
}
