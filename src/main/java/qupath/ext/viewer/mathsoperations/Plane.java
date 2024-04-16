package qupath.ext.viewer.mathsoperations;

import javafx.geometry.Point3D;

/**
 *  ax + by + cz = d
 * @param normal
 * @param d
 */
record Plane(Point3D normal, double d) {

    public static Plane createFromRectangle(Rectangle rectangle) {
        Point3D normal = rectangle.getU().crossProduct(rectangle.getV()).normalize();
        double d = normal.getX() * rectangle.p0().getX() +
                normal.getY() * rectangle.p0().getY() +
                normal.getZ() * rectangle.p0().getZ();

        return new Plane(normal, d);
    }

    public Point3D getPointOnPlane() {
        //https://stackoverflow.com/a/13490275
        return normal.multiply(d / (Math.pow(normal.getX(), 2) + Math.pow(normal.getY(), 2) + Math.pow(normal.getZ(), 2)));
    }

    public double distanceOfPoint(Point3D point) {
        //https://stackoverflow.com/a/9605695
        return point.subtract(getPointOnPlane()).dotProduct(normal);
    }
}
