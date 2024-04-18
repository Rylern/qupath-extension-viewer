package qupath.ext.viewer.mathsoperations;

import javafx.geometry.Point3D;

/**
 * A segment in the 3D space.
 *
 * @param a  the one end of the segment
 * @param b  the other end of the segment
 */
record Segment(Point3D a, Point3D b) {

    /**
     * Indicate if the distance between a point and this segment
     * is less than the given distance.
     *
     * @param point  the point whose distance with this segment should be computed
     * @param distance  the maximum distance between the point and this segment
     * @return whether the distance between the point and this segment is less than the given distance
     */
    public boolean containPoint(Point3D point, double distance) {
        double AB = a.distance(b);
        double AP = a.distance(point);
        double PB = point.distance(b);

        return Math.abs(AP + PB - AB) < distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Segment segment))
            return false;
        return segment.a.equals(a) && segment.b.equals(b) || segment.b.equals(a) && segment.a.equals(b);
    }
}
