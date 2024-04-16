package qupath.ext.viewer.scene;

import javafx.collections.ListChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import qupath.ext.viewer.extensions.Point3DExtension;
import qupath.ext.viewer.maths.Maths;
import qupath.lib.images.servers.ImageServer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Volume extends Group {

    private final ImageServer<BufferedImage> imageServer;
    private final Rectangle slicer;

    public Volume(Rectangle slicer, ImageServer<BufferedImage> imageServer) {
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
        List<Face> faces = cube.getFacesInFrontOfRectangle(getRectangle(slicer));
        Point3D centroidOfVolume = Point3DExtension.centroid(faces.stream().map(Face::getPoints).flatMap(List::stream).toList());

        return faces.stream().map(
                f -> f.computeMeshView(centroidOfVolume, imageServer)
        ).toList();
    }

    private static Maths.Rectangle getRectangle(Rectangle rectangle) {
        Point3D A = new Point3D(rectangle.getX(), rectangle.getY(), 0);
        Point3D B = new Point3D(rectangle.getX() + rectangle.getWidth(), rectangle.getY(), 0);
        Point3D C = new Point3D(rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight(), 0);

        List<Transform> transforms = new ArrayList<>(rectangle.getTransforms());
        Collections.reverse(transforms);

        for (Transform transform: transforms) {
            A = transform.transform(A);
            B = transform.transform(B);
            C = transform.transform(C);
        }

        return new Maths.Rectangle(A, B, C);
    }
}
