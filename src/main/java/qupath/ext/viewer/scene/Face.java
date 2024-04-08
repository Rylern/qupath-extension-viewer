package qupath.ext.viewer.scene;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.regions.RegionRequest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class Face {

    private final Side side;
    private final List<Point3D> points;
    public enum Side {
        FRONT,
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        BACK,
        SLICE
    }

    private Face(Side side, List<Point3D> points) {
        if (points.size() < 3) {
            throw new IllegalArgumentException("Size < 3");
        }

        this.side = side;
        this.points = points;
    }

    @Override
    public String toString() {
        return "Face{" +
                "side=" + side +
                ", points=" + points +
                '}';
    }

    public static List<Face> getFacesOfCubeInFrontOfRectangle(double width, double height, double depth, Maths.Rectangle rectangle) {
        List<Face> facesOfCube = getFacesOfCube(width, height, depth);
        List<Face> faces = new ArrayList<>();

        for (Face faceOfCube: facesOfCube) {
            List<Point3D> points = getPartOfRectangleInFrontOfOtherRectangle(
                    new Maths.Rectangle(faceOfCube.points.get(0), faceOfCube.points.get(1), faceOfCube.points.get(2)),
                    rectangle
            );
            if (points.size() > 3) {
                faces.add(new Face(faceOfCube.side, points));
            }
        }

        List<Point3D> pointsOfSlicer = facesOfCube.stream()
                .map(face -> Maths.findIntersectionLineBetweenRectangles(new Maths.Rectangle(face.points.get(0), face.points.get(1), face.points.get(2)), rectangle))
                .filter(Objects::nonNull)
                .flatMap(s -> Stream.of(s.getA(), s.getB()))
                .distinct()
                .toList();
        if (pointsOfSlicer.size() > 2) {
            faces.add(new Face(Side.SLICE, pointsOfSlicer));
        }

        return faces;
    }

    public List<Point3D> getPoints() {
        return points;
    }

    public Mesh computeMesh(Point3D centroidOfVolume) {
        List<Point3D> sortedPoints = sortPoints(points, centroidOfVolume);

        float[] vertices;
        float[] textureCoordinates;
        int[] faceIndices;

        if (sortedPoints.size() < 3) {
            vertices = new float[0];
            textureCoordinates = new float[0];
            faceIndices = new int[0];
        } else {
            Function<Point3D, Double> xTextureMapping = switch (side) {
                case FRONT, BACK -> Point3D::getX;
                case TOP, BOTTOM -> Point3D::getY;
                case LEFT, RIGHT -> Point3D::getZ;
                case SLICE -> p -> p.getX() + p.getY();         //TODO: to change
            };
            Function<Point3D, Double> yTextureMapping = switch (side) {
                case FRONT, BACK -> Point3D::getY;
                case TOP, BOTTOM -> Point3D::getZ;
                case LEFT, RIGHT -> Point3D::getX;
                case SLICE -> p -> p.getX() + p.getY();         //TODO: to change
            };

            Point3D centroid = Point3DExtension.centroid(sortedPoints);
            double minX = Point3DExtension.min(sortedPoints, xTextureMapping);
            double minY = Point3DExtension.min(sortedPoints, yTextureMapping);
            double maxX = Point3DExtension.max(sortedPoints, xTextureMapping);
            double maxY = Point3DExtension.max(sortedPoints, yTextureMapping);

            vertices = new float[(1 + sortedPoints.size()) * 3];
            textureCoordinates = new float[(1 + sortedPoints.size()) * 2];
            faceIndices = new int[sortedPoints.size() * 6];

            vertices[0] = (float) centroid.getX();
            vertices[1] = (float) centroid.getY();
            vertices[2] = (float) centroid.getZ();
            textureCoordinates[0] = (float) ((xTextureMapping.apply(centroid) - minX) / (maxX - minX));
            textureCoordinates[1] = (float) ((yTextureMapping.apply(centroid) - minY) / (maxY - minY));

            for (int i=0; i<sortedPoints.size(); i++) {
                vertices[(i+1) * 3] = (float) sortedPoints.get(i).getX();
                vertices[(i+1) * 3 + 1] = (float) sortedPoints.get(i).getY();
                vertices[(i+1) * 3 + 2] = (float) sortedPoints.get(i).getZ();

                textureCoordinates[(i+1) * 2] = (float) ((xTextureMapping.apply(sortedPoints.get(i)) - minX) / (maxX - minX));
                textureCoordinates[(i+1) * 2 + 1] = (float) ((yTextureMapping.apply(sortedPoints.get(i)) - minY) / (maxY - minY));

                faceIndices[i * 6] = 0;
                faceIndices[i * 6 + 1] = 0;
                faceIndices[i * 6 + 2] = i+1;
                faceIndices[i * 6 + 3] = i+1;
                faceIndices[i * 6 + 4] = i == sortedPoints.size()-1 ? 1 : i+2;
                faceIndices[i * 6 + 5] = i == sortedPoints.size()-1 ? 1 : i+2;
            }
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(vertices);
        mesh.getTexCoords().addAll(textureCoordinates);
        mesh.getFaces().addAll(faceIndices);

        return mesh;
    }

    public Image computeDiffuseMap(ImageServer<BufferedImage> server) throws IOException {
        BufferedImage image = switch (side) {
            case FRONT, BACK -> server.readRegion(RegionRequest.createInstance(
                    server.getPath(),
                    1.0,
                    (int) (Point3DExtension.min(points, Point3D::getX) + (float) server.getWidth()/2),
                    (int) (Point3DExtension.min(points, Point3D::getY) + (float) server.getHeight()/2),
                    (int) (Point3DExtension.max(points, Point3D::getX) - Point3DExtension.min(points, Point3D::getX)),
                    (int) (Point3DExtension.max(points, Point3D::getY) - Point3DExtension.min(points, Point3D::getY)),
                    (int) (points.get(0).getZ() + server.nZSlices()/2),
                    0
            ));
            case TOP, BOTTOM -> ImageServerExtension.getFixedY(
                    server,
                    (int) (Point3DExtension.min(points, Point3D::getX) + (float) server.getWidth()/2),
                    (int) (Point3DExtension.min(points, Point3D::getZ) + (float) server.nZSlices()/2),
                    (int) (Point3DExtension.max(points, Point3D::getX) - Point3DExtension.min(points, Point3D::getX)),
                    (int) (Point3DExtension.max(points, Point3D::getZ) - Point3DExtension.min(points, Point3D::getZ)),
                    (int) (points.get(0).getY() + server.getHeight()/2),
                    0
            );
            case LEFT, RIGHT -> ImageServerExtension.getFixedX(
                    server,
                    (int) (Point3DExtension.min(points, Point3D::getY) + (float) server.getHeight()/2),
                    (int) (Point3DExtension.min(points, Point3D::getZ) + (float) server.nZSlices()/2),
                    (int) (Point3DExtension.max(points, Point3D::getY) - Point3DExtension.min(points, Point3D::getY)),
                    (int) (Point3DExtension.max(points, Point3D::getZ) - Point3DExtension.min(points, Point3D::getZ)),
                    (int) (points.get(0).getX() + server.getWidth()/2),
                    0
            );
            case SLICE -> new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);  // TODO: change
        };


        if (image == null) {
            return null;
        } else {
            return SwingFXUtils.toFXImage(image, null);
        }
    }

    private static List<Face> getFacesOfCube(double width, double height, double depth) {
        Point3D upperLeftClose = new Point3D(-width / 2, -height / 2, -depth / 2);
        Point3D upperRightClose = new Point3D(width / 2, -height / 2, -depth / 2);
        Point3D lowerRightClose = new Point3D(width / 2, height / 2, -depth / 2);
        Point3D lowerLeftClose = new Point3D(-width / 2, height / 2, -depth / 2);
        Point3D upperLeftAway = new Point3D(-width / 2, -height / 2, depth / 2);
        Point3D upperRightAway = new Point3D(width / 2, -height / 2, depth / 2);
        Point3D lowerRightAway = new Point3D(width / 2, height / 2, depth / 2);
        Point3D lowerLeftAway = new Point3D(-width / 2, height / 2, depth / 2);

        return List.of(
                new Face(
                        Side.FRONT,
                        List.of(
                                upperLeftClose,
                                upperRightClose,
                                lowerRightClose,
                                lowerLeftClose
                        )
                ),
                new Face(
                        Side.TOP,
                        List.of(
                                upperLeftClose,
                                upperRightClose,
                                upperRightAway,
                                upperLeftAway
                        )
                ),
                new Face(
                        Side.BOTTOM,
                        List.of(
                                lowerLeftClose,
                                lowerRightClose,
                                lowerRightAway,
                                lowerLeftAway
                        )
                ),
                new Face(
                        Side.LEFT,
                        List.of(
                                upperLeftClose,
                                upperLeftAway,
                                lowerLeftAway,
                                lowerLeftClose
                        )
                ),
                new Face(
                        Side.RIGHT,
                        List.of(
                                upperRightClose,
                                upperRightAway,
                                lowerRightAway,
                                lowerRightClose
                        )
                ),
                new Face(
                        Side.BACK,
                        List.of(
                                upperLeftAway,
                                upperRightAway,
                                lowerRightAway,
                                lowerLeftAway
                        )
                )
        );
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
}
