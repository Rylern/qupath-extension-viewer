package qupath.ext.viewer;


import javafx.geometry.Point3D;

import java.util.List;
import java.util.Objects;

public class Maths {

    public static Segment findIntersectionLineBetweenRectangles(Rectangle rectangleA, Rectangle rectangleB) {
        Plane planeA = getPlane(rectangleA);
        Plane planeB = getPlane(rectangleB);

        Line intersectionLineBetweenPlanes = findIntersectionLineBetweenPlanes(planeA, planeB);

        Segment segmentA = getSegmentsOfIntersectionBetweenLineAndRectangleSegments(rectangleA, intersectionLineBetweenPlanes);
        Segment segmentB = getSegmentsOfIntersectionBetweenLineAndRectangleSegments(rectangleB, intersectionLineBetweenPlanes);

        if (segmentA == null || segmentB == null) {
            return null;
        } else {
            //TODO: improve, it should be the points of the segments that belong to both rectangles
            return segmentA;
        }
    }


    private static Plane getPlane(Rectangle rectangle) {
        Point3D normalA = rectangle.u.crossProduct(rectangle.v).normalize();
        double d = normalA.getX() * rectangle.origin.getX() + normalA.getY() * rectangle.origin.getY() + normalA.getZ() * rectangle.origin.getZ();
        return new Plane(normalA, d);
    }

    private static Line findIntersectionLineBetweenPlanes(Plane planeA, Plane planeB) {
        // https://en.wikipedia.org/wiki/Plane%E2%80%93plane_intersection
        double c1 = (planeA.d - planeB.d * planeA.normal.dotProduct(planeB.normal)) / (1 - Math.pow(planeA.normal.dotProduct(planeB.normal), 2));
        double c2 = (planeB.d - planeA.d * planeA.normal.dotProduct(planeB.normal)) / (1 - Math.pow(planeA.normal.dotProduct(planeB.normal), 2));

        return new Line(
                planeA.normal.multiply(c1).add(planeB.normal.multiply(c2)),
                planeA.normal.crossProduct(planeB.normal)
        );
    }

    private static Segment getSegmentsOfIntersectionBetweenLineAndRectangleSegments(Rectangle rectangle, Line line) {
        List<Point3D> intersectionPoints = rectangle.getSegments().stream()
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

        Point3D P1 = segment.a;
        Point3D P2 = segment.b;
        Point3D P3 = line.point;
        Point3D P4 = line.point.add(line.direction);

        List<Point3D> points = List.of(new Point3D(0, 0, 0), P1, P2, P3, P4);
        double mua = (dmnop(points, 1, 3, 4, 3) * dmnop(points, 4, 3, 2, 1) - dmnop(points, 1, 3, 2, 1) * dmnop(points, 4, 3, 4, 3)) /
                (dmnop(points, 2, 1, 2, 1) * dmnop(points, 4, 3, 4, 3) - dmnop(points, 4, 3, 2, 1) * dmnop(points, 4, 3, 2, 1));
        double mub = (dmnop(points, 1, 3, 4, 3) + mua * dmnop(points, 4, 3, 2, 1)) / dmnop(points, 4, 3, 4, 3);

        Point3D Pa = P1.add(P2.subtract(P1).multiply(mua));
        Point3D Pb = P3.add(P4.subtract(P3).multiply(mub));

        if (Pa.equals(Pb) && segment.containPoint(Pa)) {
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


    public static class Rectangle {
        private final Point3D origin;
        private final Point3D u;
        private final Point3D v;

        public Rectangle(Point3D origin, Point3D u, Point3D v) {
            this.origin = origin;
            this.u = u;
            this.v = v;
        }

        public List<Segment> getSegments() {
            return List.of(
                    new Segment(origin, origin.add(u)),
                    new Segment(origin.add(u), origin.add(u).add(v)),
                    new Segment(origin.add(u).add(v), origin.add(v)),
                    new Segment(origin.add(v), origin)
            );
        }

        @Override
        public String toString() {
            return "origin: " + origin.toString() + " u: " + u.toString() + " v: " + v.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof Rectangle rectangle))
                return false;
            return rectangle.origin.equals(origin) && rectangle.u.equals(u) && rectangle.v.equals(v);
        }
    }

    public static class Plane {
        // ax + by + cz = d
        private final Point3D normal;
        private final double d;

        public Plane(Point3D normal, double d) {
            this.normal = normal;
            this.d = d;
        }

        @Override
        public String toString() {
            return "normal: " + normal.toString() + " d: " + d;
        }
    }

    public static class Line {
        private final Point3D point;
        private final Point3D direction;

        public Line(Point3D point, Point3D normal) {
            this.point = point;
            this.direction = normal;
        }

        @Override
        public String toString() {
            return "point: " + point.toString() + " direction: " + direction.toString();
        }
    }

    public static class Segment {

        private final Point3D a;
        private final Point3D b;

        public Segment(Point3D a, Point3D b) {
            this.a = a;
            this.b = b;
        }

        public boolean containPoint(Point3D point) {
            double AB = a.distance(b);
            double AP = a.distance(point);
            double PB = point.distance(b);
            return AB == AP + PB;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof Segment segment))
                return false;
            return segment.a.equals(a) && segment.b.equals(b) || segment.b.equals(a) && segment.a.equals(b);
        }

        @Override
        public String toString() {
            return "a: " + a.toString() + " b: " + b.toString();
        }
    }
}
