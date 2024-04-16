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

class Volume extends Group {

    private final ImageServer<BufferedImage> imageServer;
    private final javafx.scene.shape.Rectangle slicer;

    public Volume(javafx.scene.shape.Rectangle slicer, ImageServer<BufferedImage> imageServer) {
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
        List<Face> faces = cube.getFacesInFrontOfRectangle(Rectangle.createFromJavaFXRectangle(slicer));
        Point3D centroidOfVolume = Point3DExtension.centroid(faces.stream().map(Face::getPoints).flatMap(List::stream).toList());

        return faces.stream().map(
                face -> face.computeMeshView(centroidOfVolume, imageServer)
        ).toList();
    }
}
