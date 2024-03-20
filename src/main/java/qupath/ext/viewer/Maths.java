package qupath.ext.viewer;


import javafx.geometry.Point3D;

import java.util.ArrayList;
import java.util.List;

public class Maths {

    public void findIntersectionLineBetweenRectangles(Rectangle rectangleA, Rectangle rectangleB) {
        Plane planeA = getPlane(rectangleA);
        Plane planeB = getPlane(rectangleB);
        Line intersectionBetweenPlanes = findIntersectionLineBetweenPlanes(planeA, planeB);

    }

    private static Plane getPlane(Rectangle rectangle) {
        Point3D normalA = rectangle.u.crossProduct(rectangle.v);
        double d = normalA.getX() * rectangle.origin.getX() + normalA.getY() * rectangle.origin.getY() + normalA.getZ() * rectangle.origin.getZ();
        return new Plane(normalA, d);
    }

    private static Line findIntersectionLineBetweenPlanes(Plane planeA, Plane planeB) {
        //https://stackoverflow.com/questions/6408670/line-of-intersection-between-two-planes/32410473#32410473
        Point3D normal = planeA.normal.crossProduct(planeB.normal);
        double det = Math.pow(normal.magnitude(), 2);

        if (det == 0) {
            return null;
        } else {
            return new Line(
                    normal.crossProduct(planeB.normal).multiply(planeA.d / det).add(planeA.normal.crossProduct(normal).multiply(planeB.d / det)),
                    normal
            );
        }
    }

    private static Point3D findIntersectionPointBetweenLines(Line lineA, Line lineB) {
        //https://stackoverflow.com/questions/2316490/the-algorithm-to-find-the-point-of-intersection-of-two-3d-line-segment

        Point3D P1 = lineA.point;
        Point3D P2 = lineA.point.add(lineA.direction);
        Point3D P3 = lineB.point;
        Point3D P4 = lineB.point.add(lineB.direction);

        List<Point3D> points = List.of(new Point3D(0, 0, 0), P1, P2, P3, P4);
        double mua = (dmnop(points, 1, 3, 4, 3) * dmnop(points, 4, 3, 2, 1) - dmnop(points, 1, 3, 2, 1) * dmnop(points, 4, 3, 4, 3)) /
                (dmnop(points, 2, 1, 2, 1) * dmnop(points, 4, 3, 4, 3) - dmnop(points, 4, 3, 2, 1) * dmnop(points, 4, 3, 2, 1));
        double mub = (dmnop(points, 1, 3, 4, 3) + mua * dmnop(points, 4, 3, 2, 1)) / dmnop(points, 4, 3, 4, 3);

        Point3D Pa = P1.add(P2.subtract(P1).multiply(mua));
        Point3D Pb = P3.add(P4.subtract(P3).multiply(mub));

        if (Pa.equals(Pb)) {
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
    }

    private static class Plane {
        // ax + by + cz = d
        private final Point3D normal;
        private final double d;

        public Plane(Point3D normal, double d) {
            this.normal = normal;
            this.d = d;
        }
    }

    private static class Line {
        private final Point3D point;
        private final Point3D direction;

        public Line(Point3D point, Point3D normal) {
            this.point = point;
            this.direction = normal;
        }
    }
}
