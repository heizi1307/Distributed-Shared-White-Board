/**
 * Author: Longxin Li
 * Student id: 1486450
 * Date: 5/16/2024
 */
package Client;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class DrawMethods {
    public static Shape drawLine(Point startPt, Point endPt) {
        return new Line2D.Double(startPt.x, startPt.y, endPt.x, endPt.y);
    }

    public static Shape drawOval(Point startPt, Point endPt) {
        return createShape(startPt, endPt, Ellipse2D.Double::new);
    }

    public static Shape drawRectangle(Point startPt, Point endPt) {
        return createShape(startPt, endPt, Rectangle::new);
    }

    public static Shape drawCircle(Point startPt, Point endPt) {
        int radius = (int) Math.sqrt(Math.pow(endPt.x - startPt.x, 2) + Math.pow(endPt.y - startPt.y, 2));
        int diameter = 2 * radius;
        int x = startPt.x - radius;
        int y = startPt.y - radius;
        return new Ellipse2D.Double(x, y, diameter, diameter);
    }

    private static Shape createShape(Point startPt, Point endPt, ShapeCreator shapeCreator) {
        int x = Math.min(startPt.x, endPt.x);
        int y = Math.min(startPt.y, endPt.y);
        int width = Math.abs(startPt.x - endPt.x);
        int height = Math.abs(startPt.y - endPt.y);
        return shapeCreator.create(x, y, width, height);
    }

    private interface ShapeCreator {
        Shape create(int x, int y, int width, int height);
    }
}
