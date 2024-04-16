package qupath.ext.viewer.extensions;

import javafx.geometry.Point3D;

import java.util.List;
import java.util.function.Function;

public class Point3DExtension {

    public static Point3D centroid(List<Point3D> points) {
        Point3D centroid = new Point3D(0, 0, 0);
        for (Point3D p: points) {
            centroid = centroid.add(p);
        }
        centroid = centroid.multiply((double) 1 / points.size());
        return centroid;
    }

    public static double min(List<Point3D> points, Function<Point3D, Double> pointToCoordinate) {
        return points.stream().mapToDouble(pointToCoordinate::apply).min().orElseThrow();
    }

    public static double max(List<Point3D> points, Function<Point3D, Double> pointToCoordinate) {
        return points.stream().mapToDouble(pointToCoordinate::apply).max().orElseThrow();
    }

    public static Point3D normal(List<Point3D> points, Point3D centroid, Point3D directionOfNormal) {
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

        if (directionOfNormal == null || largestCrossProduct.dotProduct(directionOfNormal) >= 0) {
            return largestCrossProduct;
        } else {
            return largestCrossProduct.multiply(-1);
        }
    }

    public static double signedAngle(Point3D from, Point3D to, Point3D axis) {
        return from.angle(to) * sign(axis.dotProduct(from.crossProduct(to)));
    }

    private static int sign(double a) {
        return a >= 0 ? 1 : -1;
    }
}
