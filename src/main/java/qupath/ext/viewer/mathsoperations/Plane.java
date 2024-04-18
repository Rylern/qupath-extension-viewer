package qupath.ext.viewer.mathsoperations;

import javafx.geometry.Point3D;

/**
 * A plane in the 3D space. It is described by the equation:
 * ax + by + cz = d
 * where (a, b, c) is the normal of the plane
 *
 * @param normal  (a,b,c) as described in the above equation
 * @param d  d as described in the above equation
 */
record Plane(Point3D normal, double d) {

    /**
     * Create a plane from a rectangle.
     *
     * @param rectangle  the rectangle the plane should contain
     * @return a plane that contains the rectangle
     */
    public static Plane createFromRectangle(Rectangle rectangle) {
        Point3D normal = rectangle.getU().crossProduct(rectangle.getV()).normalize();
        double d = normal.getX() * rectangle.p0().getX() +
                normal.getY() * rectangle.p0().getY() +
                normal.getZ() * rectangle.p0().getZ();

        return new Plane(normal, d);
    }

    /**
     * @return whether a point belongs to this plane
     */
    public Point3D getPointOnPlane() {
        // This function uses the algorithm described in:
        // https://stackoverflow.com/a/13490275

        return normal.multiply(d / (Math.pow(normal.getX(), 2) + Math.pow(normal.getY(), 2) + Math.pow(normal.getZ(), 2)));
    }

    /**
     * Compute the distance between a point and this plane.
     *
     * @param point  the point whose distance with this plane should be computed
     * @return the distance between the point and this plane
     */
    public double distanceOfPoint(Point3D point) {
        // This function uses the algorithm described in:
        // https://stackoverflow.com/a/9605695
        return point.subtract(getPointOnPlane()).dotProduct(normal);
    }
}
