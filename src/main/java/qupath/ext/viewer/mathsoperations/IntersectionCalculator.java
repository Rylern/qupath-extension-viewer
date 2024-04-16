package qupath.ext.viewer.mathsoperations;

import javafx.geometry.Point3D;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

class IntersectionCalculator {

    private static final double EPSILON = 0.00001;

    public static Segment findIntersectionLineBetweenRectangles(Rectangle rectangleA, Rectangle rectangleB) {
        Plane planeA = Plane.createFromRectangle(rectangleA);
        Plane planeB = Plane.createFromRectangle(rectangleB);

        Line intersectionLineBetweenPlanes = findIntersectionLineBetweenPlanes(planeA, planeB);

        Segment segmentA = getSegmentsOfIntersectionBetweenLineAndRectangleSegments(rectangleA, intersectionLineBetweenPlanes);
        Segment segmentB = getSegmentsOfIntersectionBetweenLineAndRectangleSegments(rectangleB, intersectionLineBetweenPlanes);

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

    private static Line findIntersectionLineBetweenPlanes(Plane planeA, Plane planeB) {
        // https://en.wikipedia.org/wiki/Plane%E2%80%93plane_intersection
        double c1 = (planeA.d() - planeB.d() * planeA.normal().dotProduct(planeB.normal())) / (1 - Math.pow(planeA.normal().dotProduct(planeB.normal()), 2));
        double c2 = (planeB.d() - planeA.d() * planeA.normal().dotProduct(planeB.normal())) / (1 - Math.pow(planeA.normal().dotProduct(planeB.normal()), 2));

        return new Line(
                planeA.normal().multiply(c1).add(planeB.normal().multiply(c2)),
                planeA.normal().crossProduct(planeB.normal())
        );
    }

    private static Segment getSegmentsOfIntersectionBetweenLineAndRectangleSegments(Rectangle rectangle, Line line) {
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

    private static Point3D findIntersectionPointBetweenSegmentAndLine(Segment segment, Line line) {
        //https://stackoverflow.com/questions/2316490/the-algorithm-to-find-the-point-of-intersection-of-two-3d-line-segment
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
