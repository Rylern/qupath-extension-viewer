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
    private final List<PairPoint> points;
    public enum Side {
        FRONT,
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        BACK,
        SLICE
    }
    public record PairPoint(Point3D pointInSpace, Point3D pixelCoordinate) {}

    private Face(Side side, List<PairPoint> points) {
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

    public static List<Face> getFacesOfCubeInFrontOfRectangle(ImageServer<?> imageServer, Maths.Rectangle rectangle) {
        List<Face> facesOfCube = getFacesOfCube(imageServer);
        List<Face> faces = new ArrayList<>();

        for (Face faceOfCube: facesOfCube) {
            List<Point3D> points = getPartOfRectangleInFrontOfOtherRectangle(
                    new Maths.Rectangle(faceOfCube.points.get(0).pointInSpace, faceOfCube.points.get(1).pointInSpace, faceOfCube.points.get(2).pointInSpace),
                    rectangle
            );
            if (points.size() > 3) {
                faces.add(new Face(faceOfCube.side, computePixelCoordinates(faceOfCube.points, points)));
            }
        }

        List<Point3D> pointsOfSlicer = facesOfCube.stream()
                .map(face -> Maths.findIntersectionLineBetweenRectangles(new Maths.Rectangle(
                        face.points.get(0).pointInSpace,
                        face.points.get(1).pointInSpace,
                        face.points.get(2).pointInSpace
                ), rectangle))
                .filter(Objects::nonNull)
                .flatMap(s -> Stream.of(s.getA(), s.getB()))
                .distinct()
                .toList();
        if (pointsOfSlicer.size() > 2) {
            faces.add(new Face(Side.SLICE, computePixelCoordinates(
                    facesOfCube.stream().map(f -> f.points).flatMap(List::stream).toList(),
                    pointsOfSlicer
            )));
        }

        return faces;
    }

    public List<Point3D> getPointsOfSpace() {
        return points.stream().map(PairPoint::pointInSpace).toList();
    }

    public List<Point3D> getPixelCoordinates() {
        return points.stream().map(PairPoint::pixelCoordinate).toList();
    }

    public Mesh computeMesh(Point3D centroidOfVolume) {
        List<Point3D> sortedPoints = sortPoints(getPointsOfSpace(), centroidOfVolume);

        float[] vertices;
        float[] textureCoordinates;
        int[] faceIndices;

        if (sortedPoints.size() < 3) {
            vertices = new float[0];
            textureCoordinates = new float[0];
            faceIndices = new int[0];
        } else {
            Function<Point3D, Double> xTextureMapping = switch (side) {
                case FRONT, BACK, TOP, BOTTOM -> Point3D::getX;
                case LEFT, RIGHT -> Point3D::getZ;
                case SLICE -> p -> {
                    Point3D bottomRightPoint = sortedPoints.get(0);
                    Point3D pointBeforeBottomRightPoint = sortedPoints.get(sortedPoints.size()-1);
                    Point3D pointAfterBottomRightPoint = sortedPoints.get(1);

                    for (int i=1; i<sortedPoints.size(); i++) {
                        if (sortedPoints.get(i).getY() > bottomRightPoint.getY() || (sortedPoints.get(i).getY() == bottomRightPoint.getY() && sortedPoints.get(i).getX() > bottomRightPoint.getX())) {
                            bottomRightPoint = sortedPoints.get(i);
                            pointBeforeBottomRightPoint = sortedPoints.get(i-1);
                            pointAfterBottomRightPoint = sortedPoints.get(i == sortedPoints.size()-1 ? 0 : i+1);
                        }
                    }

                    Point3D xPoint = pointBeforeBottomRightPoint.getY() > pointAfterBottomRightPoint.getY() ? pointBeforeBottomRightPoint : pointAfterBottomRightPoint;
                    Point3D xVector = xPoint.subtract(bottomRightPoint);
                    return p.dotProduct(xVector);
                };
            };
            Function<Point3D, Double> yTextureMapping = switch (side) {
                case FRONT, BACK, LEFT, RIGHT -> Point3D::getY;
                case TOP, BOTTOM -> Point3D::getZ;
                case SLICE -> p -> {
                    Point3D bottomRightPoint = sortedPoints.get(0);
                    Point3D pointBeforeBottomRightPoint = sortedPoints.get(sortedPoints.size()-1);
                    Point3D pointAfterBottomRightPoint = sortedPoints.get(1);

                    for (int i=1; i<sortedPoints.size(); i++) {
                        if (sortedPoints.get(i).getY() > bottomRightPoint.getY() || (sortedPoints.get(i).getY() == bottomRightPoint.getY() && sortedPoints.get(i).getX() > bottomRightPoint.getX())) {
                            bottomRightPoint = sortedPoints.get(i);
                            pointBeforeBottomRightPoint = sortedPoints.get(i-1);
                            pointAfterBottomRightPoint = sortedPoints.get(i == sortedPoints.size()-1 ? 0 : i+1);
                        }
                    }

                    Point3D yPoint = pointBeforeBottomRightPoint.getY() > pointAfterBottomRightPoint.getY() ? pointAfterBottomRightPoint : pointBeforeBottomRightPoint;
                    Point3D yVector = yPoint.subtract(bottomRightPoint);
                    return p.dotProduct(yVector);
                };
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
            case FRONT, BACK -> ImageServerExtension.toRGB(server.readRegion(RegionRequest.createInstance(
                    server.getPath(),
                    1.0,
                    (int) (Point3DExtension.min(getPixelCoordinates(), Point3D::getX)),
                    (int) (Point3DExtension.min(getPixelCoordinates(), Point3D::getY)),
                    (int) (Point3DExtension.max(getPixelCoordinates(), Point3D::getX) - Point3DExtension.min(getPixelCoordinates(), Point3D::getX) + 1),
                    (int) (Point3DExtension.max(getPixelCoordinates(), Point3D::getY) - Point3DExtension.min(getPixelCoordinates(), Point3D::getY) + 1),
                    (int) (points.get(0).pixelCoordinate.getZ()),
                    0
            )));
            case TOP, BOTTOM -> ImageServerExtension.toRGB(ImageServerExtension.getFixedY(
                    server,
                    (int) (Point3DExtension.min(getPixelCoordinates(), Point3D::getX)),
                    (int) (Point3DExtension.min(getPixelCoordinates(), Point3D::getZ)),
                    (int) (Point3DExtension.max(getPixelCoordinates(), Point3D::getX) - Point3DExtension.min(getPixelCoordinates(), Point3D::getX) + 1),
                    (int) (Point3DExtension.max(getPixelCoordinates(), Point3D::getZ) - Point3DExtension.min(getPixelCoordinates(), Point3D::getZ) + 1),
                    (int) (points.get(0).pixelCoordinate.getY()),
                    0
            ));
            case LEFT, RIGHT -> ImageServerExtension.toRGB(ImageServerExtension.getFixedX(
                    server,
                    (int) (Point3DExtension.min(getPixelCoordinates(), Point3D::getY)),
                    (int) (Point3DExtension.min(getPixelCoordinates(), Point3D::getZ)),
                    (int) (Point3DExtension.max(getPixelCoordinates(), Point3D::getY) - Point3DExtension.min(getPixelCoordinates(), Point3D::getY) + 1),
                    (int) (Point3DExtension.max(getPixelCoordinates(), Point3D::getZ) - Point3DExtension.min(getPixelCoordinates(), Point3D::getZ) + 1),
                    (int) (points.get(0).pixelCoordinate.getX()),
                    0
            ));
            case SLICE -> ImageServerExtension.toRGB(ImageServerExtension.getArea(server, getPixelCoordinates(), 0));
        };

        if (image == null) {
            return null;
        } else {
            return SwingFXUtils.toFXImage(image, null);
        }
    }

    private static List<Face> getFacesOfCube(ImageServer<?> imageServer) {
        PairPoint upperLeftClose = new PairPoint(
                new Point3D(
                        (double) imageServer.getPixelCalibration().getPixelWidth() * -imageServer.getWidth() / 2,
                        (double) imageServer.getPixelCalibration().getPixelHeight() * -imageServer.getHeight() / 2,
                        (double) imageServer.getPixelCalibration().getZSpacing() * -imageServer.nZSlices() / 2
                ),
                new Point3D(0, 0, 0)
        );
        PairPoint upperRightClose = new PairPoint(
                new Point3D(
                        (double) imageServer.getPixelCalibration().getPixelWidth() * imageServer.getWidth() / 2,
                        (double) imageServer.getPixelCalibration().getPixelHeight() * -imageServer.getHeight() / 2,
                        (double) imageServer.getPixelCalibration().getZSpacing() * -imageServer.nZSlices() / 2
                ),
                new Point3D(imageServer.getWidth()-1, 0, 0)
        );
        PairPoint lowerRightClose = new PairPoint(
                new Point3D(
                        (double) imageServer.getPixelCalibration().getPixelWidth() * imageServer.getWidth() / 2,
                        (double) imageServer.getPixelCalibration().getPixelHeight() * imageServer.getHeight() / 2,
                        (double) imageServer.getPixelCalibration().getZSpacing() * -imageServer.nZSlices() / 2
                ),
                new Point3D(imageServer.getWidth()-1, imageServer.getHeight()-1, 0)
        );
        PairPoint lowerLeftClose = new PairPoint(
                new Point3D(
                        (double) imageServer.getPixelCalibration().getPixelWidth() * -imageServer.getWidth() / 2,
                        (double) imageServer.getPixelCalibration().getPixelHeight() * imageServer.getHeight() / 2,
                        (double) imageServer.getPixelCalibration().getZSpacing() * -imageServer.nZSlices() / 2
                ),
                new Point3D(0, imageServer.getHeight()-1, 0)
        );
        PairPoint upperLeftAway = new PairPoint(
                new Point3D(
                        (double) imageServer.getPixelCalibration().getPixelWidth() * -imageServer.getWidth() / 2,
                        (double) imageServer.getPixelCalibration().getPixelHeight() * -imageServer.getHeight() / 2,
                        (double) imageServer.getPixelCalibration().getZSpacing() * imageServer.nZSlices() / 2
                ),
                new Point3D(0, 0, imageServer.nZSlices()-1)
        );
        PairPoint upperRightAway = new PairPoint(
                new Point3D(
                        (double) imageServer.getPixelCalibration().getPixelWidth() * imageServer.getWidth() / 2,
                        (double) imageServer.getPixelCalibration().getPixelHeight() * -imageServer.getHeight() / 2,
                        (double) imageServer.getPixelCalibration().getZSpacing() * imageServer.nZSlices() / 2
                ),
                new Point3D(imageServer.getWidth()-1, 0, imageServer.nZSlices()-1)
        );
        PairPoint lowerRightAway = new PairPoint(
                new Point3D(
                        (double) imageServer.getPixelCalibration().getPixelWidth() * imageServer.getWidth() / 2,
                        (double) imageServer.getPixelCalibration().getPixelHeight() * imageServer.getHeight() / 2,
                        (double) imageServer.getPixelCalibration().getZSpacing() * imageServer.nZSlices() / 2
                ),
                new Point3D(imageServer.getWidth()-1, imageServer.getHeight()-1, imageServer.nZSlices()-1)
        );
        PairPoint lowerLeftAway = new PairPoint(
                new Point3D(
                        (double) imageServer.getPixelCalibration().getPixelWidth() * -imageServer.getWidth() / 2,
                        (double) imageServer.getPixelCalibration().getPixelHeight() * imageServer.getHeight() / 2,
                        (double) imageServer.getPixelCalibration().getZSpacing() * imageServer.nZSlices() / 2
                ),
                new Point3D(0, imageServer.getHeight()-1, imageServer.nZSlices()-1)
        );

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

    private static List<PairPoint> computePixelCoordinates(List<PairPoint> references, List<Point3D> points) {
        List<PairPoint> uniqueReferences = references.stream().distinct().toList();

        return points.stream()
                .map(p -> {
                    for (PairPoint pairPoint : uniqueReferences) {
                        if (p.equals(pairPoint.pointInSpace)) {
                            return pairPoint;
                        }
                    }

                    List<PairPoint> pointsWithTwoCoordinateEqual = uniqueReferences.stream()
                            .filter(pairPoint -> (pairPoint.pointInSpace.getX() == p.getX() && pairPoint.pointInSpace.getY() == p.getY()) ||
                                    (pairPoint.pointInSpace.getX() == p.getX() && pairPoint.pointInSpace.getZ() == p.getZ()) ||
                                    (pairPoint.pointInSpace.getY() == p.getY() && pairPoint.pointInSpace.getZ() == p.getZ())
                            )
                            .toList();

                    if (pointsWithTwoCoordinateEqual.size() == 2) {
                        if (pointsWithTwoCoordinateEqual.get(0).pointInSpace.getX() == p.getX() &&
                                pointsWithTwoCoordinateEqual.get(0).pointInSpace.getY() == p.getY()
                        ) {
                            return new PairPoint(p, new Point3D(
                                    pointsWithTwoCoordinateEqual.get(0).pixelCoordinate.getX(),
                                    pointsWithTwoCoordinateEqual.get(0).pixelCoordinate.getY(),
                                    interpolate(p, pointsWithTwoCoordinateEqual.get(0), pointsWithTwoCoordinateEqual.get(1), Point3D::getZ)
                            ));
                        } else if (pointsWithTwoCoordinateEqual.get(0).pointInSpace.getX() == p.getX() &&
                                pointsWithTwoCoordinateEqual.get(0).pointInSpace.getZ() == p.getZ()
                        ) {
                            return new PairPoint(p, new Point3D(
                                    pointsWithTwoCoordinateEqual.get(0).pixelCoordinate.getX(),
                                    interpolate(p, pointsWithTwoCoordinateEqual.get(0), pointsWithTwoCoordinateEqual.get(1), Point3D::getY),
                                    pointsWithTwoCoordinateEqual.get(0).pixelCoordinate.getZ()
                            ));
                        } else {
                            return new PairPoint(p, new Point3D(
                                    interpolate(p, pointsWithTwoCoordinateEqual.get(0), pointsWithTwoCoordinateEqual.get(1), Point3D::getX),
                                    pointsWithTwoCoordinateEqual.get(0).pixelCoordinate.getY(),
                                    pointsWithTwoCoordinateEqual.get(0).pixelCoordinate.getZ()
                            ));
                        }
                    } else {
                        throw new RuntimeException("not happen");
                    }
                })
                .toList();
    }

    private static double interpolate(Point3D p, PairPoint p1, PairPoint p2, Function<Point3D, Double> coordinate) {
        double pixel1 = coordinate.apply(p1.pixelCoordinate);
        double pixel2 = coordinate.apply(p2.pixelCoordinate);
        double point1 = coordinate.apply(p1.pointInSpace);
        double point2 = coordinate.apply(p2.pointInSpace);

        return pixel1 + ((coordinate.apply(p) - point1) / (point2 - point1)) * (pixel2 - pixel1);
    }
}
