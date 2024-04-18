package qupath.ext.viewer.scene;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import qupath.ext.viewer.extensions.BufferedImageExtension;
import qupath.ext.viewer.extensions.ImageServerExtension;
import qupath.ext.viewer.extensions.Point3DExtension;
import qupath.ext.viewer.mathsoperations.BoundingRectangleCalculator;
import qupath.ext.viewer.mathsoperations.Rectangle;
import qupath.lib.images.servers.ImageServer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * A 2D polygon in a 3D space. It is possible to create
 * a MeshView from it.
 */
class Polygon {

    private final List<Point3D> points;
    private final Rectangle boundingRectangle;
    private final Function<Point3D, Point3D> spaceToPixelTransform;

    /**
     * Create a polygon.
     *
     * @param points  the vertices of the polygon. They don't have to be in order
     * @param spaceToPixelTransform  a function that maps a point in space to a pixel coordinate of an image
     * @throws IllegalArgumentException when the number of points is less than 3
     */
    public Polygon(List<Point3D> points, Function<Point3D, Point3D> spaceToPixelTransform) {
        if (points.size() < 3) {
            throw new IllegalArgumentException("Number of points < 3");
        }

        this.points = points;
        this.boundingRectangle = BoundingRectangleCalculator.getFromPoints(sortPoints(points, null));
        this.spaceToPixelTransform = spaceToPixelTransform;
    }

    /**
     * @return an unordered list of vertices of this polygon
     */
    public List<Point3D> getPoints() {
        return points;
    }

    /**
     * Compute a MeshView that represents this polygon.
     *
     * @param imageServer  the image this polygon should represent
     * @param centroidOfVolume  the centroid of the 3D object this polygon is part of.
     *                          This is required to correctly orient this polygon but can be null
     *                          if the orientation doesn't matter.
     * @return a MeshView that represents this polygon
     */
    public MeshView computeMeshView(ImageServer<BufferedImage> imageServer, Point3D centroidOfVolume) {
        MeshView meshView = new MeshView(computeMesh(centroidOfVolume));

        PhongMaterial material = new PhongMaterial();
        try {
            material.setDiffuseMap(computeDiffuseMap(imageServer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        meshView.setMaterial(material);

        return meshView;
    }

    /**
     * Compute the <a href="https://learnopengl.com/Lighting/Lighting-maps">diffuse map</a>
     * this polygon should display.
     *
     * @param imageServer  the image to represent
     * @return a diffuse map representing part of the image corresponding to this polygon,
     * or null if the diffuse map couldn't be computed
     * @throws IOException when an error occurs while reading the image
     */
    private Image computeDiffuseMap(ImageServer<BufferedImage> imageServer) throws IOException {
        BufferedImage image = BufferedImageExtension.toRGB(ImageServerExtension.readRegion(
                imageServer,
                new Rectangle(boundingRectangle, spaceToPixelTransform),
                0
        ));

        if (image == null) {
            return null;
        } else {
            return SwingFXUtils.toFXImage(image, null);
        }
    }

    /**
     * Compute the <a href="https://en.wikipedia.org/wiki/Polygon_mesh">mesh</a>
     * of this polygon.
     * This polygon is represented by a set of triangles. Each triangle has one vertex which is the
     * centroid of this polygon and the other two vertices are two consecutive vertices of
     * this polygon.
     *
     * @param centroidOfVolume the centroid of the 3D object this polygon is part of.
     *                         This is required to correctly orient this polygon but can be null
     *                         if the orientation doesn't matter.
     * @return the mesh this polygon represent
     */
    private Mesh computeMesh(Point3D centroidOfVolume) {
        float[] vertices;
        float[] textureCoordinates;
        int[] faceIndices;

        Function<Point3D, Double> xTextureMapping = p -> p.subtract(boundingRectangle.p0()).dotProduct(boundingRectangle.getU());
        Function<Point3D, Double> yTextureMapping = p -> p.subtract(boundingRectangle.p0()).dotProduct(boundingRectangle.getV());

        List<Point3D> points = sortPoints(this.points, centroidOfVolume);

        Point3D centroid = Point3DExtension.centroid(points);
        double minX = Point3DExtension.min(points, xTextureMapping);
        double minY = Point3DExtension.min(points, yTextureMapping);
        double maxX = Point3DExtension.max(points, xTextureMapping);
        double maxY = Point3DExtension.max(points, yTextureMapping);

        vertices = new float[(1 + points.size()) * 3];
        textureCoordinates = new float[(1 + points.size()) * 2];
        faceIndices = new int[points.size() * 6];

        vertices[0] = (float) centroid.getX();
        vertices[1] = (float) centroid.getY();
        vertices[2] = (float) centroid.getZ();
        textureCoordinates[0] = (float) ((xTextureMapping.apply(centroid) - minX) / (maxX - minX));
        textureCoordinates[1] = (float) ((yTextureMapping.apply(centroid) - minY) / (maxY - minY));

        for (int i=0; i<points.size(); i++) {
            vertices[(i+1) * 3] = (float) points.get(i).getX();
            vertices[(i+1) * 3 + 1] = (float) points.get(i).getY();
            vertices[(i+1) * 3 + 2] = (float) points.get(i).getZ();

            textureCoordinates[(i+1) * 2] = (float) ((xTextureMapping.apply(points.get(i)) - minX) / (maxX - minX));
            textureCoordinates[(i+1) * 2 + 1] = (float) ((yTextureMapping.apply(points.get(i)) - minY) / (maxY - minY));

            faceIndices[i * 6] = 0;
            faceIndices[i * 6 + 1] = 0;
            faceIndices[i * 6 + 2] = i+1;
            faceIndices[i * 6 + 3] = i+1;
            faceIndices[i * 6 + 4] = i == points.size()-1 ? 1 : i+2;
            faceIndices[i * 6 + 5] = i == points.size()-1 ? 1 : i+2;
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(vertices);
        mesh.getTexCoords().addAll(textureCoordinates);
        mesh.getFaces().addAll(faceIndices);

        return mesh;
    }

    /**
     * Sort a list of points.
     *
     * @param points  the points to sort
     * @param volumeCentroid  the centroid of the 3D object this list of points is part of.
     *                        This is required to correctly orient this list of points but can be null
     *                        if the orientation doesn't matter.
     * @return the sorted points
     */
    private static List<Point3D> sortPoints(List<Point3D> points, Point3D volumeCentroid) {
        // This function follows the method described in:
        // https://stackoverflow.com/a/20387683

        Point3D Z = Point3DExtension.centroid(points);
        Point3D n = Point3DExtension.normal(points, Z, volumeCentroid == null ? null : Z.subtract(volumeCentroid));
        Point3D ZA = points.get(0).subtract(Z);

        return points.stream()
                .sorted(Comparator.comparingDouble(p -> Point3DExtension.signedAngle(ZA, p.subtract(Z), n)))
                .toList();
    }
}
