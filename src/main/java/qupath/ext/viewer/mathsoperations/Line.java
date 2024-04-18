package qupath.ext.viewer.mathsoperations;

import javafx.geometry.Point3D;

/**
 * A line in the 3D space.
 *
 * @param point  a point belonging to the line
 * @param direction  the direction of the line
 */
record Line(Point3D point, Point3D direction) {}
