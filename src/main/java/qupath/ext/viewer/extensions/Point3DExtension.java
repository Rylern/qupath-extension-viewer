package qupath.ext.viewer.extensions;

import javafx.geometry.Point3D;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

/**
 * Provide some useful functions on {@link Point3D} objects.
 */
public class Point3DExtension {

    private Point3DExtension() {
        throw new AssertionError("This class is not instantiable.");
    }

    /**
     * Compute the centroid of a list of points.
     *
     * @param points  the points whose centroid should be computed
     * @return the centroid of the points, or (0, 0, 0) if the list is empty
     */
    public static Point3D centroid(List<Point3D> points) {
        Point3D centroid = new Point3D(0, 0, 0);
        for (Point3D p: points) {
            centroid = centroid.add(p);
        }
        centroid = centroid.multiply((double) 1 / points.size());
        return centroid;
    }

    /**
     * Compute the minimum of a list of points projected along a specified axis.
     *
     * @param points  the points whose minimum should be computed
     * @param pointToCoordinate  a function that projects a point to the desired axis
     * @return the minimum of the projected points
     */
    public static double min(List<Point3D> points, Function<Point3D, Double> pointToCoordinate) {
        return points.stream().mapToDouble(pointToCoordinate::apply).min().orElseThrow();
    }

    /**
     * Same as {@link #min(List, Function)}, but the computes the maximum instead.
     */
    public static double max(List<Point3D> points, Function<Point3D, Double> pointToCoordinate) {
        return points.stream().mapToDouble(pointToCoordinate::apply).max().orElseThrow();
    }

    /**
     * Computes the normal of a polygon described by a list of points and a centroid.
     * The desired direction of the normal can be specified.
     *
     * @param points  the list of points whose normal should be computed
     * @param centroid  the centroid of the provided list of points
     * @param directionOfNormal  the (optional, can be null) direction the normal should have
     * @return the normal of the provided points
     */
    public static Point3D normal(List<Point3D> points, Point3D centroid, Point3D directionOfNormal) {
        // This function follows the method described in https://stackoverflow.com/a/54998309

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

    /**
     * Compute the signed angle between vectors from and to in relation to axis.
     * See <a href="https://docs.unity3d.com/ScriptReference/Vector3.SignedAngle.html">this link</a>
     * for a detailed explanation.
     *
     * @param from  the direction the angle starts from
     * @param to  the direction the angle ends to
     * @param axis  the contextual direction to include in the calculation
     * @return  the angle from two vectors according to a direction
     */
    public static double signedAngle(Point3D from, Point3D to, Point3D axis) {
        return from.angle(to) * sign(axis.dotProduct(from.crossProduct(to)));
    }

    /**
     * Round a point to some precision.
     *
     * @param point  the point to round
     * @param precision  the precision (number of decimals) the result should have
     * @return the rounded point
     */
    public static Point3D round(Point3D point, int precision) {
        BigDecimal bdX = new BigDecimal(Double.toString(point.getX()));
        BigDecimal bdY = new BigDecimal(Double.toString(point.getY()));
        BigDecimal bdZ = new BigDecimal(Double.toString(point.getZ()));

        bdX = bdX.setScale(precision, RoundingMode.HALF_UP);
        bdY = bdY.setScale(precision, RoundingMode.HALF_UP);
        bdZ = bdZ.setScale(precision, RoundingMode.HALF_UP);

        return new Point3D(
                bdX.doubleValue(),
                bdY.doubleValue(),
                bdZ.doubleValue()
        );
    }

    private static int sign(double a) {
        return a >= 0 ? 1 : -1;
    }
}
