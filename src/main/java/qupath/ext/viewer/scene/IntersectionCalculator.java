package qupath.ext.viewer.scene;

import javafx.geometry.Point3D;
import javafx.scene.shape.Box;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Transform;
import qupath.lib.scripting.QP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

class IntersectionCalculator {

    public static Mesh getIntersectionMeshBetweenBoxAndRectangle(Box box, Rectangle rectangle) {
        List<Point3D> points = getIntersectionPointsBetweenBoxAndRectangle(box, rectangle);

        float[] vertices;
        float[] textureCoordinates;
        int[] faceIndices;

        if (points.isEmpty()) {
            vertices = new float[0];
            textureCoordinates = new float[0];
            faceIndices = new int[0];
        } else {
            Point3D centroid = centroid(points);

            vertices = new float[(1 + points.size()) * 3];
            textureCoordinates = new float[] {0, 0};
            faceIndices = new int[points.size() * 6];

            vertices[0] = (float) centroid.getX();
            vertices[1] = (float) centroid.getY();
            vertices[2] = (float) centroid.getZ();

            for (int i=0; i<points.size(); i++) {
                vertices[(i+1) * 3] = (float) points.get(i).getX();
                vertices[(i+1) * 3 + 1] = (float) points.get(i).getY();
                vertices[(i+1) * 3 + 2] = (float) points.get(i).getZ();

                faceIndices[i * 6] = 0;
                faceIndices[i * 6 + 1] = 0;
                faceIndices[i * 6 + 2] = i+1;
                faceIndices[i * 6 + 3] = 0;
                faceIndices[i * 6 + 4] = i == points.size()-1 ? 1 : i+2;
                faceIndices[i * 6 + 5] = 0;
            }
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(vertices);
        mesh.getTexCoords().addAll(textureCoordinates);
        mesh.getFaces().addAll(faceIndices);

        return mesh;
    }

    private static List<Point3D> getIntersectionPointsBetweenBoxAndRectangle(Box box, Rectangle rectangle) {
        Maths.Rectangle rect = getRectangle(rectangle);

        return sortPoints(getRectanglesOfBox(box).stream()
                .map(r -> Maths.findIntersectionLineBetweenRectangles(r, rect))
                .filter(Objects::nonNull)
                .flatMap(s -> Stream.of(s.getA(), s.getB()))
                .distinct()
                .toList());
    }

    private static List<Point3D> sortPoints(List<Point3D> points) {
        //https://stackoverflow.com/questions/20387282/compute-the-cross-section-of-a-cube?fbclid=IwAR1a5zUPQOaICBawb7Wy1aymAGvoX97wELTFij1kYfC5Z-zvNph9ftWdr4s

        if (points.size() < 3) {
            return List.of();
        } else {
            Point3D Z = centroid(points);
            Point3D n = normal(points, Z);
            Point3D ZA = points.get(0).subtract(Z);

            return points.stream()
                    .sorted(Comparator.comparingDouble(p -> signedAngle(ZA, p.subtract(Z), n)))
                    .toList();
        }
    }

    private static List<Maths.Rectangle> getRectanglesOfBox(Box box) {
        Point3D upperLeftClose = new Point3D(-box.getWidth() / 2, -box.getHeight() / 2, -box.getDepth() / 2);
        Point3D upperRightClose = new Point3D(box.getWidth() / 2, -box.getHeight() / 2, -box.getDepth() / 2);
        Point3D lowerRightClose = new Point3D(box.getWidth() / 2, box.getHeight() / 2, -box.getDepth() / 2);
        Point3D lowerLeftClose = new Point3D(-box.getWidth() / 2, box.getHeight() / 2, -box.getDepth() / 2);
        Point3D upperLeftAway = new Point3D(-box.getWidth() / 2, -box.getHeight() / 2, box.getDepth() / 2);
        Point3D upperRightAway = new Point3D(box.getWidth() / 2, -box.getHeight() / 2, box.getDepth() / 2);
        Point3D lowerRightAway = new Point3D(box.getWidth() / 2, box.getHeight() / 2, box.getDepth() / 2);
        Point3D lowerLeftAway = new Point3D(-box.getWidth() / 2, box.getHeight() / 2, box.getDepth() / 2);

        for (Transform transform: box.getTransforms()) {
            upperLeftClose = transform.transform(upperLeftClose);
            upperRightClose = transform.transform(upperRightClose);
            lowerRightClose = transform.transform(lowerRightClose);
            lowerLeftClose = transform.transform(lowerLeftClose);
            upperLeftAway = transform.transform(upperLeftAway);
            upperRightAway = transform.transform(upperRightAway);
            lowerRightAway = transform.transform(lowerRightAway);
            lowerLeftAway = transform.transform(lowerLeftAway);
        }

        return List.of(
                // front
                new Maths.Rectangle(
                        upperLeftClose,
                        upperRightClose,
                        lowerRightClose
                ),
                // top
                new Maths.Rectangle(
                        upperLeftClose,
                        upperRightClose,
                        upperRightAway
                ),
                // bottom
                new Maths.Rectangle(
                        lowerLeftClose,
                        lowerRightClose,
                        lowerRightAway
                ),
                // left
                new Maths.Rectangle(
                        upperLeftClose,
                        upperLeftAway,
                        lowerLeftAway
                ),
                // right
                new Maths.Rectangle(
                        upperRightClose,
                        upperRightAway,
                        lowerRightAway
                ),
                // back
                new Maths.Rectangle(
                        upperLeftAway,
                        upperRightAway,
                        lowerRightAway
                )
        );
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

    private static Point3D normal(List<Point3D> points, Point3D centroid) {
        /*
        //https://stackoverflow.com/questions/32274127/how-to-efficiently-determine-the-normal-to-a-polygon-in-3d-space
        Point3D sum = new Point3D(0, 0, 0);

        for (int i=0; i<points.size(); i++) {
            sum.add(points.get(i).subtract(centroid).crossProduct(points.get(i == points.size()-1 ? 0 : i+1).subtract(centroid)));
        }
        return sum.normalize();

         */
        //https://stackoverflow.com/a/54998309
        Point3D largestCrossProduct = new Point3D(0,0, 0);
        double largestCrossProductMagnitude = 0;

        for (int i=0; i<points.size(); i++) {
            for (int j=i+1; j<points.size(); j++) {
                Point3D crossProduct = points.get(i).subtract(centroid).crossProduct(points.get(j).subtract(centroid));
                double magnitude = crossProduct.magnitude();
                if (magnitude > largestCrossProductMagnitude) {
                    largestCrossProduct = crossProduct;
                    largestCrossProductMagnitude = magnitude;
                }
            }
        }

        return largestCrossProduct;

    }

    private static double signedAngle(Point3D from, Point3D to, Point3D axis) {
        return from.angle(to) * sign(axis.dotProduct(from.crossProduct(to)));
    }

    private static Point3D centroid(List<Point3D> points) {
        Point3D centroid = new Point3D(0, 0, 0);
        for (Point3D p: points) {
            centroid = centroid.add(p);
        }
        centroid = centroid.multiply((double) 1 / points.size());
        return centroid;

        /*
        return points.stream()
                .skip(1) // Skip the first point since it's already assigned
                .reduce(points.get(0), Point3D::add)
                .multiply((double) 1 / points.size());

         */
    }

    private static int sign(double a) {
        return a >= 0 ? 1 : -1;
    }
}
