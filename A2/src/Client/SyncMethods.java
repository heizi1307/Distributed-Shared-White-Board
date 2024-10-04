/**
 * Author: Longxin Li
 * Student id: 1486450
 * Date: 5/16/2024
 */
package Client;

import Controller.MessageFormat;

import java.awt.*;
import java.rmi.RemoteException;
import java.util.Hashtable;

public class SyncMethods {
    /**
     * Handles drawing on the whiteboard.
     *
     * @param message      The message containing drawing information.
     * @param user         The user performing the drawing action.
     * @param point        The current point of the drawing action.
     * @param stratPoints  Hashtable containing start points for drawing actions.
     * @param clientMethods The client methods instance.
     * @throws RemoteException If a remote exception occurs.
     */
    public static void handleDrawing(MessageFormat message, String user, Point point, Hashtable<String, Point> stratPoints, ClientMethods clientMethods) throws RemoteException {
        Point startPt = stratPoints.get(user);
        Graphics2D g2d = clientMethods.getGraphics2D();

        g2d.setPaint(message.getColor());
        g2d.setStroke(new BasicStroke(message.getWeight()));

        if (message.getMode().equals("eraser")) {
            g2d.setStroke(new BasicStroke(message.getWeight()));
        }

        Shape shape = DrawMethods.drawLine(startPt, point);
        stratPoints.put(user, point);

        g2d.draw(shape);
        clientMethods.repaint();
    }

    /**
     * Handles the end of a drawing action.
     *
     * @param message      The message containing drawing information.
     * @param user         The user who performed the drawing action.
     * @param point        The final point of the drawing action.
     * @param stratPoints  Hashtable containing start points for drawing actions.
     * @param clientMethods The client methods instance.
     * @throws RemoteException If a remote exception occurs.
     */
    public static void handleEnd(MessageFormat message, String user, Point point, Hashtable<String, Point> stratPoints, ClientMethods clientMethods) throws RemoteException {
        Point startPt = stratPoints.get(user);
        Graphics2D g2d = clientMethods.getGraphics2D();

        g2d.setPaint(message.getColor());
        g2d.setStroke(new BasicStroke(message.getWeight()));

        Shape shape = null;
        switch (message.getMode()) {
            case "draw":
            case "line":
                shape = DrawMethods.drawLine(startPt, point);
                break;
            case "eraser":
                g2d.setStroke(new BasicStroke(message.getWeight()));
                break;
            case "rectangle":
                shape = DrawMethods.drawRectangle(startPt, point);
                break;
            case "oval":
                shape = DrawMethods.drawOval(startPt, point);
                break;
            case "circle":
                shape = DrawMethods.drawCircle(startPt, point);
                break;
            case "text":
                g2d.setColor(clientMethods.getColor());
                g2d.setFont(new Font("TimesRoman", Font.PLAIN, 15));
                g2d.drawString(message.getText(), point.x, point.y);
                break;
        }

        if (shape != null) {
            g2d.draw(shape);
        }

        clientMethods.repaint();
        stratPoints.remove(user);
    }
}
