package qupath.ext.viewer.scene;


import javafx.geometry.Point3D;

import java.util.List;
import java.util.Objects;

class Maths {

    private static final double EPSILON = 0.00001;

    public static Segment findIntersectionLineBetweenRectangles(Rectangle rectangleA, Rectangle rectangleB) {
        Plane planeA = rectangleA.getPlane();
        Plane planeB = rectangleB.getPlane();

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

    public static Point3D findIntersectionPointBetweenLineAndPlane(Line line, Plane plane) {
        // https://stackoverflow.com/a/52711312
        if (plane.normal.dotProduct(line.direction.normalize()) == 0) {
            return null;
        }

        double t = (plane.normal.dotProduct(plane.getPointOnPlane()) - plane.normal.dotProduct(line.point)) / plane.normal.dotProduct(line.direction.normalize());
        return line.point.add(line.direction.normalize().multiply(t));
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

        if (Pa.distance(Pb) < EPSILON && segment.containPoint(Pa)) {
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

        /**
         * params in order
         *
         * @param p0
         * @param p1
         * @param p2
         */
        public Rectangle(Point3D p0, Point3D p1, Point3D p2) {
            this.origin = p0;
            this.u = p1.subtract(p0);
            this.v = p2.subtract(p1);
        }

        public List<Segment> getSegments() {
            return List.of(
                    new Segment(origin, origin.add(u)),
                    new Segment(origin.add(u), origin.add(u).add(v)),
                    new Segment(origin.add(u).add(v), origin.add(v)),
                    new Segment(origin.add(v), origin)
            );
        }

        public List<Point3D> getPoints() {
            return List.of(
                    origin,
                    origin.add(u),
                    origin.add(u).add(v),
                    origin.add(v)
            );
        }

        public Plane getPlane() {
            Point3D normalA = u.crossProduct(v).normalize();
            double d = normalA.getX() * origin.getX() + normalA.getY() * origin.getY() + normalA.getZ() * origin.getZ();
            return new Plane(normalA, d);
        }

        public static List<Maths.Rectangle> getRectanglesOfImage(double width, double height, double depth) {
            Point3D upperLeftClose = new Point3D(-width / 2, -height / 2, -depth / 2);
            Point3D upperRightClose = new Point3D(width / 2, -height / 2, -depth / 2);
            Point3D lowerRightClose = new Point3D(width / 2, height / 2, -depth / 2);
            Point3D lowerLeftClose = new Point3D(-width / 2, height / 2, -depth / 2);
            Point3D upperLeftAway = new Point3D(-width / 2, -height / 2, depth / 2);
            Point3D upperRightAway = new Point3D(width / 2, -height / 2, depth / 2);
            Point3D lowerRightAway = new Point3D(width / 2, height / 2, depth / 2);
            Point3D lowerLeftAway = new Point3D(-width / 2, height / 2, depth / 2);

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

        public Point3D getPointOnPlane() {
            //https://stackoverflow.com/a/13490275
            return normal.multiply(d / (Math.pow(normal.getX(), 2) + Math.pow(normal.getY(), 2) + Math.pow(normal.getZ(), 2)));
        }

        public double distanceOfPoint(Point3D point) {
            //https://stackoverflow.com/a/9605695
            return point.subtract(getPointOnPlane()).dotProduct(normal);
        }

        @Override
        public String toString() {
            return "normal: " + normal.toString() + " d: " + d;
        }
    }

    public static class Line {
        private final Point3D point;
        private final Point3D direction;

        public Line(Point3D point, Point3D direction) {
            this.point = point;
            this.direction = direction;
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

            return Math.abs(AP + PB - AB) < EPSILON;    // equivalent to AP + PB = AB
        }

        public Point3D getA() {
            return a;
        }

        public Point3D getB() {
            return b;
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
