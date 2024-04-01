package qupath.ext.viewer.scene;

import javafx.geometry.Point3D;

import java.util.List;

public class Point3DExtension {

    public static Point3D centroid(List<Point3D> points) {
        Point3D centroid = new Point3D(0, 0, 0);
        for (Point3D p: points) {
            centroid = centroid.add(p);
        }
        centroid = centroid.multiply((double) 1 / points.size());
        return centroid;
    }

    public static Point3D minXY(List<Point3D> points) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;

        for (Point3D p: points) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
        }
        return new Point3D(minX, minY, 0);
    }

    public static Point3D maxXY(List<Point3D> points) {
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Point3D p: points) {
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
        }
        return new Point3D(maxX, maxY, 0);
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

        if (largestCrossProduct.dotProduct(directionOfNormal) >= 0) {
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
