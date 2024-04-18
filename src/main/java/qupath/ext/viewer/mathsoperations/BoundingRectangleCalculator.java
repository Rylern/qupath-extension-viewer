package qupath.ext.viewer.mathsoperations;

import javafx.geometry.Point3D;

import java.util.List;

/**
 * Compute the minimum area rectangle containing a set of 3D points using the
 * algorithm in <a href="https://www.geometrictools.com/Documentation/MinimumAreaRectangle.pdf">this document</a>
 * (listing 1).
 */
public class BoundingRectangleCalculator {

    private BoundingRectangleCalculator() {
        throw new AssertionError("This class is not instantiable.");
    }

    /**
     * Compute the minimum area rectangle containing a set of points.
     *
     * @param points the points that must be contained in the rectangle
     * @return the minimum area rectangle containing the points
     */
    public static Rectangle getFromPoints(List<Point3D> points) {
        // This function follows the method described in:
        // https://www.geometrictools.com/Documentation/MinimumAreaRectangle.pdf

        double minArea = Double.POSITIVE_INFINITY;
        Rectangle boundingRectangle = null;

        for (int i0 = points.size()-1, i1 = 0; i1 < points.size(); i0 = i1++) {
            Point3D origin = points.get(i0);
            Point3D U0 = points.get(i1).subtract(origin).normalize();
            Point3D U1 = perpendicular(U0, points);

            double min0 = 0;
            double max0 = 0;
            double max1 = 0;
            for (Point3D point : points) {
                Point3D D = point.subtract(origin);

                double dot = U0.dotProduct(D);
                if (dot < min0) {
                    min0 = dot;
                } else if (dot > max0) {
                    max0 = dot;
                }

                dot = U1.dotProduct(D);
                if (dot > max1) {
                    max1 = dot;
                }
            }

            double area = (max0 - min0) * max1;
            if (area < minArea) {
                minArea = area;
                boundingRectangle = new Rectangle(
                        origin.add(U0.multiply(min0)),
                        origin.add(U0.multiply(max0)),
                        origin.add(U0.multiply(max0)).add(U1.multiply(max1))
                );
            }
        }

        return boundingRectangle;
    }

    private static Point3D perpendicular(Point3D v, List<Point3D> pointsOfPlane) {
        Point3D perpendicularOfPlane = pointsOfPlane.get(0).subtract(pointsOfPlane.get(1)).crossProduct(pointsOfPlane.get(2).subtract(pointsOfPlane.get(1)));
        return v.crossProduct(perpendicularOfPlane).normalize();
    }
}
