package qupath.ext.viewer.mathsoperations;

import javafx.geometry.Point3D;

record Segment(Point3D a, Point3D b) {

    public boolean containPoint(Point3D point, double precision) {
        double AB = a.distance(b);
        double AP = a.distance(point);
        double PB = point.distance(b);

        return Math.abs(AP + PB - AB) < precision;    // equivalent to AP + PB = AB
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
