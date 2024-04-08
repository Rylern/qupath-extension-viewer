package qupath.ext.viewer.scene;

import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Transform;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.regions.RegionRequest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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
        getChildren().setAll(getMeshes2());
    }

    private List<MeshView> getMeshes2() {
        List<Face> faces = Face.getFacesOfCubeInFrontOfRectangle(imageServer.getWidth(), imageServer.getHeight(), imageServer.nZSlices(), getRectangle(slicer));
        Point3D centroidOfVolume = Point3DExtension.centroid(faces.stream().map(Face::getPoints).flatMap(List::stream).toList());

        return faces.stream()
                .map(face -> {
                    MeshView meshView = new MeshView(face.computeMesh(centroidOfVolume));

                    PhongMaterial material = new PhongMaterial();
                    try {
                        material.setDiffuseMap(face.computeDiffuseMap(imageServer));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    meshView.setMaterial(material);

                    return meshView;
                })
                .toList();
    }

    private List<MeshView> getMeshes() {
        return getMeshesOfBoxInFrontOfRectangle().stream()
                .map(mesh -> {
                    MeshView meshView = new MeshView(mesh);
                    PhongMaterial material = new PhongMaterial();

                    try {
                        material.setDiffuseMap(SwingFXUtils.toFXImage(imageServer.readRegion(RegionRequest.createInstance(imageServer)), null));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    meshView.setMaterial(material);
                    return meshView;
                })
                .toList();
    }

    private List<Mesh> getMeshesOfBoxInFrontOfRectangle() {
        List<List<Point3D>> pointsOfVolume = Stream.concat(
                Maths.Rectangle.getRectanglesOfImage(imageServer.getWidth(), imageServer.getHeight(), imageServer.nZSlices()).stream()
                        .map(r -> getPartOfRectangleInFrontOfOtherRectangle(r, getRectangle(slicer))),
                Stream.of(getIntersectionPointsBetweenBoxAndRectangle())
        ).toList();

        Point3D centroidOfVolume = Point3DExtension.centroid(pointsOfVolume.stream().flatMap(List::stream).toList());

        return pointsOfVolume.stream()
                .map(points -> getMeshOfPolygon(points, centroidOfVolume))
                .toList();
    }

    private List<Point3D> getIntersectionPointsBetweenBoxAndRectangle() {
        return Maths.Rectangle.getRectanglesOfImage(imageServer.getWidth(), imageServer.getHeight(), imageServer.nZSlices()).stream()
                .map(r -> Maths.findIntersectionLineBetweenRectangles(r, getRectangle(slicer)))
                .filter(Objects::nonNull)
                .flatMap(s -> Stream.of(s.getA(), s.getB()))
                .distinct()
                .toList();
    }

    private static Mesh getMeshOfPolygon(List<Point3D> points, Point3D volumeCentroid) {
        List<Point3D> pointSorted = sortPoints(points, volumeCentroid);

        float[] vertices;
        float[] textureCoordinates;
        int[] faceIndices;

        if (pointSorted.size() < 3) {
            vertices = new float[0];
            textureCoordinates = new float[0];
            faceIndices = new int[0];
        } else {
            Point3D centroid = Point3DExtension.centroid(pointSorted);
            Point3D min = Point3DExtension.minXY(pointSorted);
            Point3D max = Point3DExtension.maxXY(pointSorted);

            vertices = new float[(1 + pointSorted.size()) * 3];
            textureCoordinates = new float[(1 + pointSorted.size()) * 2];
            faceIndices = new int[pointSorted.size() * 6];

            vertices[0] = (float) centroid.getX();
            vertices[1] = (float) centroid.getY();
            vertices[2] = (float) centroid.getZ();
            textureCoordinates[0] = (float) ((centroid.getX() - min.getX()) / (max.getX() - min.getX()));
            textureCoordinates[1] = (float) ((centroid.getY() - min.getY()) / (max.getY() - min.getY()));

            for (int i=0; i<pointSorted.size(); i++) {
                vertices[(i+1) * 3] = (float) pointSorted.get(i).getX();
                vertices[(i+1) * 3 + 1] = (float) pointSorted.get(i).getY();
                vertices[(i+1) * 3 + 2] = (float) pointSorted.get(i).getZ();

                textureCoordinates[(i+1) * 2] = (float) ((pointSorted.get(i).getX() - min.getX()) / (max.getX() - min.getX()));
                textureCoordinates[(i+1) * 2 + 1] = (float) ((pointSorted.get(i).getY() - min.getY()) / (max.getY() - min.getY()));

                faceIndices[i * 6] = 0;
                faceIndices[i * 6 + 1] = 0;
                faceIndices[i * 6 + 2] = i+1;
                faceIndices[i * 6 + 3] = i+1;
                faceIndices[i * 6 + 4] = i == pointSorted.size()-1 ? 1 : i+2;
                faceIndices[i * 6 + 5] = i == pointSorted.size()-1 ? 1 : i+2;
            }
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(vertices);
        mesh.getTexCoords().addAll(textureCoordinates);
        mesh.getFaces().addAll(faceIndices);

        return mesh;
    }

    private static List<Point3D> sortPoints(List<Point3D> points, Point3D volumeCentroid) {
        //https://stackoverflow.com/questions/20387282/compute-the-cross-section-of-a-cube?fbclid=IwAR1a5zUPQOaICBawb7Wy1aymAGvoX97wELTFij1kYfC5Z-zvNph9ftWdr4s
        if (points.size() < 3) {
            return List.of();
        } else {
            Point3D Z = Point3DExtension.centroid(points);
            Point3D n = Point3DExtension.normal(points, Z, Z.subtract(volumeCentroid));
            Point3D ZA = points.get(0).subtract(Z);

            return points.stream()
                    .sorted(Comparator.comparingDouble(p -> Point3DExtension.signedAngle(ZA, p.subtract(Z), n)))
                    .toList();
        }
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

    private static List<Point3D> getPartOfRectangleInFrontOfOtherRectangle(Maths.Rectangle rectangleToCut, Maths.Rectangle rectangle) {
        List<Point3D> pointsOfRectangleInFrontOfPlane = getPointsOfRectangleInFrontOfPlane(rectangleToCut, rectangle.getPlane());
        Maths.Segment segment = Maths.findIntersectionLineBetweenRectangles(rectangleToCut, rectangle);

        if (segment == null) {
            return pointsOfRectangleInFrontOfPlane;
        } else {
            return Stream.concat(
                    pointsOfRectangleInFrontOfPlane.stream(),
                    Stream.of(segment.getA(), segment.getB())
            ).toList();
        }
    }

    private static List<Point3D> getPointsOfRectangleInFrontOfPlane(Maths.Rectangle rectangle, Maths.Plane plane) {
        return rectangle.getPoints().stream()
                .filter(p -> plane.distanceOfPoint(p) > 0)
                .toList();
    }
}
