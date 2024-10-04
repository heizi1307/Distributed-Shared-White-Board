/**
 * Author: Longxin Li
 * Student id: 1486450
 * Date: 5/16/2024
 */
package Client;

import Controller.ServerInterface;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;

public class ClientMethods extends JComponent {
    private static final long serialVersionUID = 1L;
    private String Username;
    private boolean flag; // if is manager
    private Point startPt, endPt;
    private Color color;
    private String mode;
    private String text;
    private BufferedImage image;
    private BufferedImage SyncImage;
    private Graphics2D graphics2D;
    private ServerInterface serverInterface;
    private static float weight = 2.0f;


    public ClientMethods(String name, boolean flag, ServerInterface serverInterface) {
        // Initialize properties
        this.serverInterface = serverInterface;
        this.color = Color.BLACK;
        this.text = "";
        this.flag = flag;
        this.mode = "draw"; // Default mode is drawing
        this.Username = name;
        setDoubleBuffered(false); // Disable double buffering for immediate drawing updates

        // Mouse listener for handling mouse press events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPt = e.getPoint(); // Record the starting point of the mouse press
                saveWhiteBoard(); // Save the current state of the whiteboard
                setSender("start"); // Set the sender to indicate the start of drawing
            }
        });

        // Mouse motion listener for handling mouse drag events
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                endPt = e.getPoint(); // Record the ending point of the mouse drag
                Shape shape = null; // Initialize the shape to be drawn

                if (graphics2D != null) {
                    switch (mode) {
                        case "draw":
                            // Draw freehand lines
                            shape = drawLine(startPt, endPt); // Create a line shape
                            graphics2D.setPaint(color); // Set the color
                            graphics2D.setStroke(new BasicStroke(weight)); // Set the stroke weight
                            startPt = endPt; // Update the starting point for the next segment
                            try {
                                // Send drawing message to the server
                                MessageFormat messageFormat = new MessageFormat("drawing", Username, mode, color, endPt, "", weight);
                                serverInterface.sendMessageToServer(messageFormat);
                            } catch (RemoteException exception) {
                                JOptionPane.showMessageDialog(null, "Lost Connection."); // Handle RemoteException
                            }
                            break;
                        case "eraser":
                            // Erase freehand lines (draw with white color)
                            shape = drawLine(startPt, endPt); // Create a line shape
                            startPt = endPt; // Update the starting point for the next segment
                            graphics2D.setPaint(Color.WHITE); // Set white color for erasing
                            graphics2D.setStroke(new BasicStroke(weight)); // Set the stroke weight
                            try {
                                // Send erasing message to the server
                                MessageFormat messageFormat = new MessageFormat("drawing", Username, mode, Color.WHITE, endPt, "", weight);
                                serverInterface.sendMessageToServer(messageFormat);
                            } catch (RemoteException exception) {
                                JOptionPane.showMessageDialog(null, "Lost Connection."); // Handle RemoteException
                            }
                            break;
                        case "line":
                            // Draw straight lines
                            drawImage(); // Draw the current image to avoid overwriting previous shapes
                            shape = drawLine(startPt, endPt); // Create a line shape
                            graphics2D.setPaint(color); // Set the color
                            graphics2D.setStroke(new BasicStroke(weight)); // Set the stroke weight
                            break;
                        case "circle":
                            // Draw circles
                            drawImage(); // Draw the current image to avoid overwriting previous shapes
                            shape = drawCircle(startPt, endPt); // Create a circle shape
                            graphics2D.setPaint(color); // Set the color
                            graphics2D.setStroke(new BasicStroke(weight)); // Set the stroke weight
                            break;
                        case "oval":
                            // Draw ovals
                            drawImage(); // Draw the current image to avoid overwriting previous shapes
                            shape = drawOval(startPt, endPt); // Create an oval shape
                            graphics2D.setPaint(color); // Set the color
                            graphics2D.setStroke(new BasicStroke(weight)); // Set the stroke weight
                            break;
                        case "rectangle":
                            // Draw rectangles
                            drawImage(); // Draw the current image to avoid overwriting previous shapes
                            shape = drawRectangle(startPt, endPt); // Create a rectangle shape
                            graphics2D.setPaint(color); // Set the color
                            graphics2D.setStroke(new BasicStroke(weight)); // Set the stroke weight
                            break;
                        case "text":
                            // Add text to the whiteboard
                            drawImage(); // Draw the current image to avoid overwriting previous shapes
                            graphics2D.setPaint(color); // Set the color
                            graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 20)); // Set the font
                            graphics2D.drawString("Type your text", endPt.x, endPt.y); // Draw placeholder text
                            shape = drawText(startPt); // Create a text shape
                            graphics2D.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)); // Set stroke
                            break;
                    }
                    graphics2D.draw(shape); // Draw the shape on the graphics context
                    repaint(); // Request a repaint to update the display
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                endPt = e.getPoint(); // Record the end point of the mouse release
                Shape shape; // Initialize the shape to be drawn

                if (graphics2D != null) {
                    switch (mode) {
                        case "draw":
                        case "line":
                        case "circle":
                        case "oval":
                        case "rectangle":
                            // Finalize the shape (e.g., line, circle, oval, rectangle)
                            shape = drawShape(startPt, endPt); // Create the shape
                            try {
                                graphics2D.draw(shape); // Draw the shape on the graphics context
                            } catch (NullPointerException ignored) {
                                // Handle NullPointerException if graphics2D is null
                            }
                            break;
                        case "text":
                            // Handle text input
                            text = JOptionPane.showInputDialog("Type your text"); // Prompt the user to enter text
                            drawImage(); // Draw the current image to avoid overwriting previous shapes
                            graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 20)); // Set the font
                            graphics2D.drawString(text, endPt.x, endPt.y); // Draw the entered text
                            graphics2D.setStroke(new BasicStroke(1.0f)); // Set the stroke
                            break;
                    }
                    repaint(); // Request a repaint to update the display

                    try {
                        // Send an "end" message to the server to synchronize with other clients
                        MessageFormat messageFormat = new MessageFormat("end", Username, mode, color, endPt, text, weight);
                        serverInterface.sendMessageToServer(messageFormat);
                    } catch (RemoteException exception) {
                        JOptionPane.showMessageDialog(null, "Lost connection."); // Handle RemoteException
                    }
                }
            }
        });
    }

    private Shape drawShape(Point startPt, Point endPt) {
        // Switch based on the drawing mode
        return switch (mode) {
            case "draw", "line" -> drawLine(startPt, endPt); // If mode is "draw" or "line", draw a line
            case "circle" -> drawCircle(startPt, endPt); // If mode is "circle", draw a circle
            case "oval" -> drawOval(startPt, endPt); // If mode is "oval", draw an oval
            case "rectangle" -> drawRectangle(startPt, endPt); // If mode is "rectangle", draw a rectangle
            default -> null; // Return null for unknown modes
        };
    }


    public void drawImage() {
        drawImage(SyncImage);
    }

    protected void paintComponent(Graphics graphic) {
        super.paintComponent(graphic); // Call the parent class's method to ensure correct rendering

        // Check if the image is null
        if (image == null) {
            int width = getWidth(); // Get the width of the whiteboard
            int height = getHeight(); // Get the height of the whiteboard

            // If this client is the manager
            if (flag) {
                // Create a new BufferedImage with the dimensions of the whiteboard
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                graphics2D = (Graphics2D) image.getGraphics();
                // Enable anti-aliasing for smoother graphics
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Reset the whiteboard
                reset();
            } else {
                try {
                    // Request the whiteboard image from the server
                    byte[] rawImage = serverInterface.sendWhiteBoard();
                    // Load the image into the BufferedImage
                    image = ImageIO.read(new ByteArrayInputStream(rawImage));
                    graphics2D = (Graphics2D) image.getGraphics();
                    // Enable anti-aliasing for smoother graphics
                    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Set the paint color
                    graphics2D.setPaint(color);
                } catch (IOException e) {
                    // Handle error if fail to load image
                    System.err.println("Fail to load image.");
                }
            }
        }

        // Draw the image onto the component
        graphic.drawImage(image, 0, 0, null);
    }


    public void setColor(Color color){
        this.color = color;
    }

    public Color getColor(){
        return color;
    }

    public void drawImage(BufferedImage image) {
        graphics2D.drawImage(image, null, 0, 0);
        repaint();
    }

    public Shape drawLine(Point startPt, Point endPt) {
        return new Line2D.Double(startPt.x, startPt.y, endPt.x, endPt.y);
    }

    public Shape drawOval(Point startPt, Point endPt) {
        return createShape(startPt, endPt, Ellipse2D.Double::new);
    }

    public Shape drawRectangle(Point startPt, Point endPt) {
        return createShape(startPt, endPt, Rectangle::new);
    }

    public Shape drawCircle(Point startPt, Point endPt) {
        int radius = (int) Math.sqrt(Math.pow(endPt.x - startPt.x, 2) + Math.pow(endPt.y - startPt.y, 2));
        int diameter = 2 * radius;
        int x = startPt.x - radius;
        int y = startPt.y - radius;
        return new Ellipse2D.Double(x, y, diameter, diameter);
    }

    public Shape drawText(Point startPt) {
        int x = startPt.x - 5;
        int y = startPt.y - 20;
        int width = 130;
        int height = 25;
        return new RoundRectangle2D.Double(x, y, width, height, 15, 15);
    }

    public void setWeight(Float weight) {
        ClientMethods.weight = weight;
    }

    public static float getWeight(){
        return ClientMethods.weight;
    }


    private interface ShapeCreator {
        Shape create(int x, int y, int width, int height);
    }

    private Shape createShape(Point startPt, Point endPt, ShapeCreator shapeCreator) {
        int x = Math.min(startPt.x, endPt.x);
        int y = Math.min(startPt.y, endPt.y);
        int width = Math.abs(startPt.x - endPt.x);
        int height = Math.abs(startPt.y - endPt.y);
        return shapeCreator.create(x, y, width, height);
    }

    private void setSender(String action) {
        try {
            MessageFormat messageFormat = new MessageFormat(action, Username, mode, color, startPt, text, weight);
            serverInterface.sendMessageToServer(messageFormat);
        } catch (RemoteException exception) {
            JOptionPane.showMessageDialog(null, "Lost connection.");
        }
    }

    public void saveWhiteBoard() {
        ColorModel colorModel = image.getColorModel();
        WritableRaster raster = image.copyData(null);
        SyncImage = new BufferedImage(colorModel, raster, false, null);
    }


    public String getMode() {
        return mode;
    }

    public Graphics2D getGraphics2D() {
        return graphics2D;
    }

    public BufferedImage getImage() {
        saveWhiteBoard();
        return SyncImage;
    }

    public void reset() {
        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillRect(0, 0, 700, 350);
        graphics2D.setPaint(color);
        repaint();
    }

    public void draw() {
        mode = "draw";
    }

    public void line() {
        mode = "line";
    }

    public void rectangle() {
        mode = "rectangle";
    }

    public void circle() {
        mode = "circle";
    }

    public void oval() {
        mode = "oval";
    }

    public void text() {
        mode = "text";
    }

    public void eraser() {
        mode = "eraser";
    }

}
