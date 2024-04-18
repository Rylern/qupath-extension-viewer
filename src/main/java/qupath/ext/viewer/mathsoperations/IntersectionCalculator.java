package qupath.ext.viewer.mathsoperations;

import javafx.geometry.Point3D;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Compute the intersection segment between two rectangles in the 3D space.
 */
class IntersectionCalculator {

    private static final double EPSILON = 0.00001;

    private IntersectionCalculator() {
        throw new AssertionError("This class is not instantiable.");
    }

    /**
     * Compute the intersection segment between two rectangles in the 3D space.
     * If such segment doesn't exit, null is returned.
     *
     * @param rectangleA  the first rectangle
     * @param rectangleB  the second rectangle
     * @return the intersection between the two rectangles, or null if it doesn't exist
     */
    public static Segment findIntersectionLineBetweenRectangles(Rectangle rectangleA, Rectangle rectangleB) {
        Plane planeA = Plane.createFromRectangle(rectangleA);
        Plane planeB = Plane.createFromRectangle(rectangleB);

        Line intersectionLineBetweenPlanes = findIntersectionLineBetweenPlanes(planeA, planeB);

        Segment segmentA = getSegmentsOfIntersectionBetweenLineAndRectangleSides(rectangleA, intersectionLineBetweenPlanes);
        Segment segmentB = getSegmentsOfIntersectionBetweenLineAndRectangleSides(rectangleB, intersectionLineBetweenPlanes);

        if (segmentA == null || segmentB == null) {
            return null;
        } else {
            List<Point3D> pointsBelongingToBothSegments = Stream.of(segmentA.a(), segmentA.b(), segmentB.a(), segmentB.b())
                    .filter(p -> segmentA.containPoint(p, EPSILON) && segmentB.containPoint(p, EPSILON))
                    .toList();

            if (pointsBelongingToBothSegments.size() == 2) {
                return new Segment(pointsBelongingToBothSegments.get(0), pointsBelongingToBothSegments.get(1));
            } else {
                return null;
            }
        }
    }

    /**
     * Compute the intersection line between two planes.
     * The planes must not be parallel (an exception will occur in such case).
     *
     * @param planeA  the first plane
     * @param planeB  the second plane
     * @return the intersection line between the two planes
     */
    private static Line findIntersectionLineBetweenPlanes(Plane planeA, Plane planeB) {
        // This function uses the formula stated in:
        // https://en.wikipedia.org/wiki/Plane%E2%80%93plane_intersection

        double c1 = (planeA.d() - planeB.d() * planeA.normal().dotProduct(planeB.normal())) / (1 - Math.pow(planeA.normal().dotProduct(planeB.normal()), 2));
        double c2 = (planeB.d() - planeA.d() * planeA.normal().dotProduct(planeB.normal())) / (1 - Math.pow(planeA.normal().dotProduct(planeB.normal()), 2));

        return new Line(
                planeA.normal().multiply(c1).add(planeB.normal().multiply(c2)),
                planeA.normal().crossProduct(planeB.normal())
        );
    }

    /**
     * Compute the segment that intersects both a side of a provided rectangle and a provided line.
     * If such segment doesn't exist, null is returned.
     *
     * @param rectangle  the rectangle whose sides should intersect with the line
     * @param line  the line that should intersect with one side of the rectangle
     * @return the segment that intersects both a side of the rectangle and the line,
     * or null if it doesn't exist
     */
    private static Segment getSegmentsOfIntersectionBetweenLineAndRectangleSides(Rectangle rectangle, Line line) {
        List<Point3D> intersectionPoints = Stream.of(
                        new Segment(rectangle.p0(), rectangle.p1()),
                        new Segment(rectangle.p1(), rectangle.p2()),
                        new Segment(rectangle.p2(), rectangle.p3()),
                        new Segment(rectangle.p3(), rectangle.p0())
                )
                .map(segment -> findIntersectionPointBetweenSegmentAndLine(segment, line))
                .filter(Objects::nonNull)
                .toList();

        if (intersectionPoints.size() == 2) {
            return new Segment(intersectionPoints.get(0), intersectionPoints.get(1));
        } else {
            return null;
        }
    }

    /**
     * Compute the intersection point between a segment and a line. If such point
     * doesn't exist, null is returned.
     *
     * @param segment  the segment that should intersect the line
     * @param line  the line that should intersect the segment
     * @return the intersection point between the line and the segment, or null if it
     * doesn't exist
     */
    private static Point3D findIntersectionPointBetweenSegmentAndLine(Segment segment, Line line) {
        // This function uses the algorithm described in
        // https://paulbourke.net/geometry/pointlineplane/ (section "The shortest line between two lines in 3D")
        if (segment == null || line == null) {
            return null;
        }

        Point3D P1 = segment.a();
        Point3D P2 = segment.b();
        Point3D P3 = line.point();
        Point3D P4 = line.point().add(line.direction());

        List<Point3D> points = List.of(new Point3D(0, 0, 0), P1, P2, P3, P4);
        double mua = (dmnop(points, 1, 3, 4, 3) * dmnop(points, 4, 3, 2, 1) - dmnop(points, 1, 3, 2, 1) * dmnop(points, 4, 3, 4, 3)) /
                (dmnop(points, 2, 1, 2, 1) * dmnop(points, 4, 3, 4, 3) - dmnop(points, 4, 3, 2, 1) * dmnop(points, 4, 3, 2, 1));
        double mub = (dmnop(points, 1, 3, 4, 3) + mua * dmnop(points, 4, 3, 2, 1)) / dmnop(points, 4, 3, 4, 3);

        Point3D Pa = P1.add(P2.subtract(P1).multiply(mua));
        Point3D Pb = P3.add(P4.subtract(P3).multiply(mub));

        if (Pa.distance(Pb) < EPSILON && segment.containPoint(Pa, EPSILON)) {
            return Pa;
        } else {
            return null;
        }
    }

    private static double dmnop(List<Point3D> points, int m, int n, int o, int p) {
        return (points.get(m).getX() - points.get(n).getX()) * (points.get(o).getX() - points.get(p).getX()) +
                (points.get(m).getY() - points.get(n).getY()) * (points.get(o).getY() - points.get(p).getY()) +
                (points.get(m).getZ() - points.get(n).getZ()) * (points.get(o).getZ() - points.get(p).getZ());
    }
}
