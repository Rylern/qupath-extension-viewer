package qupath.ext.viewer.scene;

import javafx.collections.ListChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Transform;
import qupath.ext.viewer.extensions.Point3DExtension;
import qupath.ext.viewer.mathsoperations.Rectangle;
import qupath.lib.images.servers.ImageServer;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Represent the image as a 3D volume.
 * A rectangle can slice this volume, so that its inside
 * can be seen.
 */
class Volume extends Group {

    private final ImageServer<BufferedImage> imageServer;
    private final javafx.scene.shape.Rectangle slicer;

    /**
     * Create the volume.
     *
     * @param imageServer  the image this volume should represent
     * @param slicer  a rectangle that should slice this volume
     */
    public Volume(ImageServer<BufferedImage> imageServer, javafx.scene.shape.Rectangle slicer) {
        this.imageServer = imageServer;
        this.slicer = slicer;

        draw();
        slicer.getTransforms().addListener((ListChangeListener<? super Transform>) change -> draw());
    }

    private void draw() {
        getChildren().setAll(getMeshes());
    }

    private List<MeshView> getMeshes() {
        Cube cube = new Cube(imageServer);
        List<Polygon> polygons = cube.getPartOfCubeInFrontOfRectangle(Rectangle.createFromJavaFXRectangle(slicer));
        Point3D centroidOfVolume = Point3DExtension.centroid(polygons.stream().map(Polygon::getPoints).flatMap(List::stream).toList());

        return polygons.stream().map(
                polygon -> polygon.computeMeshView(imageServer, centroidOfVolume)
        ).toList();
    }
}
