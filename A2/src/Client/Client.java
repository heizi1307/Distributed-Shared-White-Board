/**
 * Author: Longxin Li
 * Student id: 1486450
 * Date: 5/16/2024
 */
package Client;

import Controller.ClinetInterface;
import Controller.MessageFormat;
import Controller.ServerInterface;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class Client extends UnicastRemoteObject implements ClinetInterface {
    // Reference to the server interface for communication
    static ServerInterface serverInterface;

    // Map to store colors by name
    private static Map<String, Color> colorMap;

    // Indicates whether the user is a manager
    private boolean flag;

    // Indicates whether the user has permission
    private boolean permiFlag;

    // Reference to the main application window
    private JFrame frame;

    // List model to store the usernames of connected users
    private final DefaultListModel<String> usersList;

    // List model to store chat messages
    private final DefaultListModel<String> chats;

    // Buttons for various drawing actions
    private JButton drawBtn, lineBtn, rectBtn, circleBtn, ovalBtn, textBtn, eraserBtn, widthBtn, clearBtn, saveBtn, saveAsBtn, openBtn, colorBtn;

    // Scroll pane for displaying chat messages
    private JScrollPane msgArea;

    // List component for displaying chat messages
    private JList<String> chat;

    // List to store buttons for managing their properties
    private final ArrayList<JButton> btnList;

    // Reference to client methods for drawing actions
    static ClientMethods clientMethods;

    // User's name
    private String userName;

    // Name of the picture being worked on
    private String pictureName;

    // Path for saving images
    private String savePath;

    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Borders for active and inactive buttons
            LineBorder empty = new LineBorder(new Color(238, 238, 238));
            LineBorder box = new LineBorder(Color.BLACK);

            // Method to set button borders
            ActionListener setButtonBorder = event -> {
                for (JButton button : btnList) {
                    button.setBorder(button == event.getSource() ? box : empty);
                }
            };

            // Handling different button events
            if (e.getSource() == clearBtn) { // Clear button clicked
                clientMethods.reset(); // Reset client drawing area
                if (flag) { // If user is a manager
                    try {
                        serverInterface.refresh(); // Refresh the whiteboard for all users
                    } catch (RemoteException exception) {
                        JOptionPane.showMessageDialog(null, "Server is closed, please save and exit.");
                    }
                }
            } else if (e.getSource() == openBtn) { // Open button clicked
                try {
                    open(); // Open an image
                } catch (IOException exception) {
                    System.err.println("IO error");
                }
            } else if (e.getSource() == saveBtn) { // Save button clicked
                try {
                    save(); // Save the current image
                } catch (IOException exception) {
                    System.err.println("IO error");
                }
            } else if (e.getSource() == saveAsBtn) { // SaveAs button clicked
                try {
                    saveAs(); // Save the current image with a new name
                } catch (IOException exception) {
                    System.err.println("IO error");
                }
            } else if (e.getSource() == drawBtn) { // Draw button clicked
                clientMethods.draw(); // Start drawing mode
                setButtonBorder.actionPerformed(e); // Set button border
            } else if (e.getSource() == rectBtn) { // Rectangle button clicked
                clientMethods.rectangle(); // Start drawing rectangles
                setButtonBorder.actionPerformed(e); // Set button border
            } else if (e.getSource() == circleBtn) { // Circle button clicked
                clientMethods.circle(); // Start drawing circles
                setButtonBorder.actionPerformed(e); // Set button border
            } else if (e.getSource() == ovalBtn) { // Oval button clicked
                clientMethods.oval(); // Start drawing ovals
                setButtonBorder.actionPerformed(e); // Set button border
            } else if (e.getSource() == textBtn) { // Text button clicked
                clientMethods.text(); // Start drawing text
                setButtonBorder.actionPerformed(e); // Set button border
            } else if (e.getSource() == eraserBtn) { // Eraser button clicked
                clientMethods.eraser(); // Start erasing
                setButtonBorder.actionPerformed(e); // Set button border
            } else if (e.getSource() == lineBtn) { // Line button clicked
                clientMethods.line(); // Start drawing lines
                setButtonBorder.actionPerformed(e); // Set button border
            } else if (e.getSource() == widthBtn) { // Width button clicked
                String inputStr = JOptionPane.showInputDialog(null, "Set the weight, now is " + clientMethods.getWeight());
                if (inputStr != null) {
                    try {
                        Float input = Float.valueOf(inputStr);
                        clientMethods.setWeight(input); // Set the line weight
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.");
                    }
                    setButtonBorder.actionPerformed(e); // Set button border
                }
            }
        }
    };
    // Hashtable to store the starting points of drawing actions for each user
    private final Hashtable<String, Point> stratPoints = new Hashtable<String, Point>();

    // Constructor for the Client class
    // Throws RemoteException as it extends UnicastRemoteObject
    protected Client() throws RemoteException {
        // Initialize the list model for storing usernames of connected users
        usersList = new DefaultListModel<>();
        // Initialize flag indicating if the user is a manager (default: false)
        flag = false;
        // Initialize permiFlag indicating if the user has permission (default: true)
        permiFlag = true;
        // Initialize the list model for storing chat messages
        chats = new DefaultListModel<>();
        // Initialize the list to store buttons for managing their properties
        btnList = new ArrayList<>();
    }

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        if (args.length < 2) {
            System.out.println("Usage: java -jar CreateWhiteBoard.jar <serverIPAddress> <serverPort>");
            return;
        }

        String hostName = args[0];
        String portNumberStr = args[1];

        // Check if the port number is numeric
        if (!portNumberStr.matches("\\d+")) {
            System.out.println("Port number must be numeric.");
            return;
        }


        // Build the server address using localhost, port 1307, and server name WhiteBoardServer
        String serverAddress = Connector.buildServerAddress(hostName, portNumberStr, "WhiteBoardServer");

        // Look up the server interface using the server address
        serverInterface = Connector.lookupServerInterface(serverAddress);

        // Create a new instance of the Client class
        ClinetInterface clientInterface = new Client();

        // Log in and register the client with the server
        Connector.loginAndRegister(clientInterface, serverInterface);

        // Draw the whiteboard user interface
        clientInterface.whiteBoardUI(serverInterface);

        // Initialize the color map for the application
        initColorMap();
    }


    private static void initColorMap() {
        colorMap = new HashMap<>();
        colorMap.put("Red", Color.RED);
        colorMap.put("Orange", Color.ORANGE);
        colorMap.put("Yellow", Color.YELLOW);
        colorMap.put("Green", Color.GREEN);
        colorMap.put("Cyan", Color.CYAN);
        colorMap.put("Blue", Color.BLUE);
        colorMap.put("Magenta", Color.MAGENTA);
        colorMap.put("Pink", Color.PINK);
        colorMap.put("Gray", Color.GRAY);
        colorMap.put("Dark Gray", Color.DARK_GRAY);
        colorMap.put("Light Gray", Color.LIGHT_GRAY);
        colorMap.put("Black", Color.BLACK);
        colorMap.put("White", Color.WHITE);
        colorMap.put("Brown", new Color(139, 69, 19)); // Brown
        colorMap.put("Purple", new Color(128, 0, 128)); // Purple
        colorMap.put("Teal", new Color(0, 128, 128)); // Teal
    }

    @Override
    public void setManager() throws RemoteException {
        this.flag = true;
    }

    @Override
    public boolean wantToJoin(String name) {
        return JOptionPane.showConfirmDialog(frame, name + " wants to share your whiteboard.", "Allowed.", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    @Override
    public void setFlag(boolean flag) {
        this.permiFlag = flag;
    }

    @Override
    public boolean getPermit() {
        return this.permiFlag;
    }

    @Override
    public boolean getManager() {
        return this.flag;
    }

    @Override
    public String getUserName() {
        return this.userName;
    }

    @Override
    public void setUserName(String name) {
        this.userName = name;
    }

    @Override
    public void updateUserSet(Set<ClinetInterface> clientSet) throws RemoteException {
        // Remove all existing elements from the users list
        this.usersList.removeAllElements();

        // Iterate through the set of client interfaces
        for (ClinetInterface i : clientSet) {
            // Add the username of each client to the users list
            usersList.addElement(i.getUserName());
        }
    }


    @Override
    public void syncWhiteBoard(MessageFormat message) throws RemoteException {
        // If the message's username matches the current user's name, return without taking any action
        if (message.getUserName().compareTo(userName) == 0) {
            return;
        }

        // Extract user and point information from the message
        String user = message.getUserName();
        Point point = message.getPoint();

        // Process the message based on its status
        switch (message.getStatus()) {
            case "start" -> {
                // If the status is "start", store the starting point for the user's drawing action
                stratPoints.put(user, point);
            }
            case "drawing" -> {
                // If the status is "drawing", handle the drawing action
                SyncMethods.handleDrawing(message, user, point, stratPoints, clientMethods);
            }
            case "end" -> {
                // If the status is "end", handle the end of the drawing action
                SyncMethods.handleEnd(message, user, point, stratPoints, clientMethods);
            }
        }
    }


    @Override
    public void refreshWhiteBoard() {
        if (!this.flag) {
            clientMethods.reset();
        }
    }

    @Override
    public void closeUI() {
        if (!this.permiFlag) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "You don't have access permit.", "Warning", JOptionPane.WARNING_MESSAGE);
                    System.exit(0);
                }
            });
            t.start();
            return;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, "Lost connection to the server.", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        });
        t.start();
    }


    @Override
    public void stratChat(String text) {
        this.chats.addElement(text);
    }

    @Override
    public byte[] sendWhiteBoard() throws IOException {
        // Create a byte array output stream to store the image data
        ByteArrayOutputStream imageArray = new ByteArrayOutputStream();

        // Write the current whiteboard image to the output stream in PNG format
        ImageIO.write(clientMethods.getImage(), "png", imageArray);

        // Convert the image data stored in the output stream to a byte array and return it
        return imageArray.toByteArray();
    }

    @Override
    public void drawOpenedImage(byte[] rawImage) throws IOException {
        // Read the image from the provided raw image data
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(rawImage));

        // Draw the image on the whiteboard using the clientMethods instance
        clientMethods.drawImage(image);
    }

    @Override
    public void whiteBoardUI(ServerInterface serverInterface) {
        // Create a new JFrame for the whiteboard UI with the user's name
        frame = new JFrame(userName + "'s WhiteBoard");

        // Get the content pane of the JFrame
        Container container = frame.getContentPane();

        // Create an instance of ClientMethods to handle client-specific methods
        clientMethods = new ClientMethods(userName, flag, serverInterface);

        // Set the maximum size of the clientMethods panel within the JFrame
        clientMethods.setMaximumSize(new Dimension(500, 350));

        // Create a line border with black color for UI components
        LineBorder lineBorder = new LineBorder(Color.BLACK);

        // Create a JButton for selecting colors
        colorBtn = new JButton("Color");
        colorBtn.setBorder(lineBorder);
        colorBtn.setOpaque(true);

        // Add action listeners for the color button
        colorBtn.addActionListener(actionListener);
        colorBtn.addActionListener(e -> {
            // Get the button's parent container
            JButton button = (JButton) e.getSource();
            Container container1 = button.getParent();

            // Show a color chooser dialog to select a color
            Color selectedColor = JColorChooser.showDialog(container1, "Choose a Color", Color.WHITE);

            // Set the background color of the button to the selected color
            if (selectedColor != null) {
                button.setBackground(selectedColor);
                // Set the selected color for drawing on the whiteboard
                clientMethods.setColor(selectedColor);
            }
        });


        // Create a JButton for drawing
        drawBtn = new JButton("Draw");
        drawBtn.setToolTipText("Draw");
        drawBtn.setBorder(lineBorder); // Set the border style
        drawBtn.addActionListener(actionListener); // Add action listener for drawing
        lineBorder = new LineBorder(new Color(238, 238, 238));

        // Create a JButton for drawing lines
        lineBtn = new JButton("Line");
        lineBtn.setToolTipText("Line");
        lineBtn.setBorder(lineBorder); // Set the border style
        lineBtn.addActionListener(actionListener); // Add action listener for drawing lines

        // Create a JButton for drawing rectangles
        rectBtn = new JButton("Rectangle");
        rectBtn.setToolTipText("Rectangle");
        rectBtn.setBorder(lineBorder); // Set the border style
        rectBtn.addActionListener(actionListener); // Add action listener for drawing rectangles

        // Create a JButton for drawing circles
        circleBtn = new JButton("Circle");
        circleBtn.setToolTipText("Circle");
        circleBtn.setBorder(lineBorder); // Set the border style
        circleBtn.addActionListener(actionListener); // Add action listener for drawing circles

        // Create a JButton for drawing ovals
        ovalBtn = new JButton("Oval");
        ovalBtn.setToolTipText("Oval");
        ovalBtn.setBorder(lineBorder); // Set the border style
        ovalBtn.addActionListener(actionListener); // Add action listener for drawing ovals

        // Create a JButton for adding text
        textBtn = new JButton("Text");
        textBtn.setToolTipText("Text");
        textBtn.setBorder(lineBorder); // Set the border style
        textBtn.addActionListener(actionListener); // Add action listener for adding text

        // Create a JButton for using the eraser
        eraserBtn = new JButton("Eraser");
        eraserBtn.setToolTipText("Eraser");
        eraserBtn.setBorder(lineBorder); // Set the border style
        eraserBtn.addActionListener(actionListener); // Add action listener for using the eraser

        // Create a JButton for adjusting line width
        widthBtn = new JButton("Width");
        widthBtn.setToolTipText("Width");
        widthBtn.setBorder(lineBorder); // Set the border style
        widthBtn.addActionListener(actionListener); // Add action listener for adjusting line width


        // Add all drawing buttons to the list of buttons
        btnList.add(drawBtn);
        btnList.add(lineBtn);
        btnList.add(rectBtn);
        btnList.add(circleBtn);
        btnList.add(ovalBtn);
        btnList.add(textBtn);
        btnList.add(widthBtn);
        btnList.add(eraserBtn);

        // Create buttons for clearing the board, saving, and opening images
        clearBtn = createButton("Clean Board", "Clean all", actionListener, true);
        saveBtn = createButton("Save Image", "Save as image", actionListener, true);
        saveAsBtn = createButton("Save as", "Save image file", actionListener, true);
        openBtn = createButton("Open Image", "Open the Image file", actionListener, true);

        // Hide the buttons for users who are not the manager
        if (!flag) {
            clearBtn.setVisible(false);
            saveBtn.setVisible(false);
            saveAsBtn.setVisible(false);
            openBtn.setVisible(false);
        }


        // Create a JList to display the list of users
        JList<String> list = new JList<>(usersList);

        // Create a scroll pane to contain the list
        JScrollPane jScrollPane = new JScrollPane(list);

        // Set the maximum size of the scroll pane
        jScrollPane.setMaximumSize(new Dimension(100, 300));

        // If the user is not a manager, adjust the maximum size of the scroll pane
        if (!flag) {
            jScrollPane.setMaximumSize(new Dimension(100, 300));
        }

        // If the user is a manager, add a mouse listener to enable kicking users
        if (flag) {
            addKickUserListener(list);
        }


        // Create a JList to display chat messages
        chat = new JList<>(chats);

// Create a scroll pane to contain the chat list
        msgArea = new JScrollPane(chat);

// Set the maximum size of the scroll pane
        msgArea.setMaximumSize(new Dimension(200, 100));

// Create a text field for entering new chat messages
        JTextField msgText = new JTextField();

// Set the maximum size of the text field
        msgText.setMaximumSize(new Dimension(200, 20));

// Create a button for sending messages
        JButton sendBtn = new JButton("Send");

// Add a mouse listener to the send button to handle message sending
        sendBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Check if the message text field is not empty
                if (!msgText.getText().isEmpty()) {
                    try {
                        // Send the chat message to the server
                        serverInterface.startChat(userName + ":" + msgText.getText());

                        // Scroll to the bottom of the chat area to show the latest message
                        SwingUtilities.invokeLater(() -> {
                            JScrollBar vertical = msgArea.getVerticalScrollBar();
                            vertical.setValue(vertical.getMaximum());
                        });
                    } catch (RemoteException exception) {
                        // Show a message dialog if the server is closed
                        JOptionPane.showMessageDialog(null, "Server closed, please save and exit");
                    }

                    // Clear the message text field after sending the message
                    msgText.setText("");
                }
            }
        });


        // Create a GroupLayout and set it as the layout manager for the container
        GroupLayout layout = new GroupLayout(container);
        container.setLayout(layout);

        // Automatically create gaps between components and containers
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Define the horizontal layout of components
        layout.setHorizontalGroup(layout.createSequentialGroup()
                // Group the components horizontally
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        // Add buttons for drawing tools
                        .addComponent(drawBtn)
                        .addComponent(lineBtn)
                        .addComponent(rectBtn)
                        .addComponent(circleBtn)
                        .addComponent(ovalBtn)
                        .addComponent(textBtn)
                        .addComponent(eraserBtn)
                        .addComponent(widthBtn)
                        .addComponent(colorBtn)
                        .addComponent(clearBtn)
                        .addComponent(openBtn)
                        .addComponent(saveBtn)
                        .addComponent(saveAsBtn)
                )
                // Group the client methods panel
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(clientMethods)
                )
                // Group the chat-related components
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(jScrollPane)
                        .addComponent(msgArea)
                        .addComponent(msgText)
                        .addComponent(sendBtn)
                )
        );

        // Define the vertical layout of components
        layout.setVerticalGroup(layout.createSequentialGroup()
                // Group the components vertically
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        // Group the drawing tool buttons horizontally
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(drawBtn)
                                .addComponent(lineBtn)
                                .addComponent(rectBtn)
                                .addComponent(circleBtn)
                                .addComponent(ovalBtn)
                                .addComponent(textBtn)
                                .addComponent(eraserBtn)
                                .addComponent(widthBtn)
                                .addComponent(colorBtn)
                                .addComponent(clearBtn)
                                .addComponent(openBtn)
                                .addComponent(saveBtn)
                                .addComponent(saveAsBtn)
                        )
                        // Add the client methods panel
                        .addComponent(clientMethods)
                        // Group the chat-related components vertically
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane)
                                .addComponent(msgArea)
                                .addComponent(msgText)
                                .addComponent(sendBtn)
                        )
                        // Add an empty group to maintain spacing
                        .addGroup(layout.createSequentialGroup())
                )
        );

        // Link the sizes of certain components horizontally
        layout.linkSize(SwingConstants.HORIZONTAL, clearBtn, saveBtn, saveAsBtn, openBtn);

        // Set the minimum size of the frame
        frame.setMinimumSize(new Dimension(820, 500));

        // Center the frame on the screen
        frame.setLocationRelativeTo(null);

        // Set the close operation of the frame
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Make the frame visible
        frame.setVisible(true);


        // Add a window listener to the frame
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            // Override the windowClosing method to handle the window close event
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Check if the user is a manager
                if (flag) {
                    // If the user is a manager, prompt to close the server
                    if (JOptionPane.showConfirmDialog(frame, "Are you sure to close the server?", "Close the Server now?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        try {
                            // Remove all clients from the server and exit
                            serverInterface.removeAllClient();
                        } catch (IOException e) {
                            // Handle IO error
                            System.out.println("IO Error");
                        } finally {
                            // Exit the application
                            System.exit(0);
                        }
                    }
                } else {
                    // If the user is not a manager, prompt to quit the WhiteBoard
                    if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to quit?", "Close the WhiteBoard", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        try {
                            // Notify the server about the user's exit and update the user set
                            serverInterface.exitClient(userName);
                            updateUserSet(serverInterface.getClients());
                            try {
                                // Send the chat message to the server
                                serverInterface.startChat(userName + " has quited.");

                                // Scroll to the bottom of the chat area to show the latest message
                                SwingUtilities.invokeLater(() -> {
                                    JScrollBar vertical = msgArea.getVerticalScrollBar();
                                    vertical.setValue(vertical.getMaximum());
                                });
                            } catch (RemoteException exception) {
                                // Show a message dialog if the server is closed
                                JOptionPane.showMessageDialog(null, "Server closed, please save and exit");
                            }
                        } catch (RemoteException exception) {
                            // Handle RemoteException
                            JOptionPane.showMessageDialog(null, "Server closed, please save and exit.");
                        } finally {
                            // Exit the application
                            System.exit(0);
                        }
                    }
                }
            }
        });
    }

    private JButton createButton(String text, String toolTip, ActionListener actionListener, boolean useLineBorder) {
        // Create a new JButton with the specified text
        JButton button = new JButton(text);

        // Set the tooltip if provided
        if (toolTip != null) {
            button.setToolTipText(toolTip);
        }

        // Set the border style based on the flag 'useLineBorder'
        if (useLineBorder) {
            // If 'useLineBorder' is true, set a LineBorder with black color
            button.setBorder(new LineBorder(Color.BLACK));
        } else {
            // If 'useLineBorder' is false, remove the border and make the button opaque
            button.setBorderPainted(false);
            button.setOpaque(true);
        }

        // Add the ActionListener to the button
        button.addActionListener(actionListener);

        // Return the created button
        return button;
    }


    private void addKickUserListener(JList<String> list) {
        // Add a mouse listener to the JList
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get the source of the event, which should be the JList
                JList<String> list1 = (JList<String>) e.getSource();

                // Check if the event is a double-click
                if (e.getClickCount() == 2) {
                    // Get the index of the clicked item in the list
                    int i = list1.locationToIndex(e.getPoint());

                    // Get the selected user from the model
                    String selectedUser = list1.getModel().getElementAt(i);

                    // Check if the selected user is not the current user
                    try {
                        if (!getUserName().equals(selectedUser)) {
                            // Show a confirmation dialog to confirm kicking the user
                            int dialogResult = JOptionPane.showConfirmDialog(frame, "Are you sure to remove " + selectedUser + "?", "Warning", JOptionPane.YES_NO_OPTION);

                            // If the user confirms, kick the selected user
                            if (dialogResult == JOptionPane.YES_OPTION) {
                                try {
                                    serverInterface.kickClient(selectedUser);
                                    updateUserSet(serverInterface.getClients());
                                    try {
                                        // Send the chat message to the server
                                        serverInterface.startChat(selectedUser + " has kicked by manager.");

                                        // Scroll to the bottom of the chat area to show the latest message
                                        SwingUtilities.invokeLater(() -> {
                                            JScrollBar vertical = msgArea.getVerticalScrollBar();
                                            vertical.setValue(vertical.getMaximum());
                                        });
                                    } catch (RemoteException exception) {
                                        // Show a message dialog if the server is closed
                                        JOptionPane.showMessageDialog(null, "Server closed, please save and exit");
                                    }
                                } catch (IOException exception) {
                                    System.out.println("IO error");
                                }
                            }
                        }
                    } catch (HeadlessException exception) {
                        System.out.println("Headless error");
                    }
                }
            }
        });
    }


    private void open() throws IOException {
        // Prompt the user to select an image file
        File imageFile = selectImageFile();

        // If an image file is selected
        if (imageFile != null) {
            // Read the image from the selected file
            BufferedImage image = readImageFromFile(imageFile);

            // If the image is successfully read
            if (image != null) {
                // Display the image on the whiteboard
                clientMethods.drawImage(image);

                // Convert the image to a byte array
                byte[] imageData = convertImageToByteArray(image);

                // Send the image data to the server
                serverInterface.sendOpenedImage(imageData);
            }
        }
    }


    // Method to open a file dialog for selecting an image file
    private File selectImageFile() {
        // Create a file dialog for opening an image file
        FileDialog openDialog = new FileDialog(frame, "Open Image", FileDialog.LOAD);

        // Display the file dialog
        openDialog.setVisible(true);

        // Return the selected file if one is selected, otherwise return null
        return (openDialog.getFile() != null) ? new File(openDialog.getDirectory(), openDialog.getFile()) : null;
    }

    // Method to read an image from a file and convert it to a BufferedImage
    private BufferedImage readImageFromFile(File file) {
        try {
            // Read the image from the specified file
            return ImageIO.read(file);
        } catch (IOException e) {
            // Print error message if there's an exception
            System.err.println("Error reading image: " + e.getMessage());
            return null; // Return null if there's an error
        }
    }

    // Method to convert a BufferedImage to a byte array
    private byte[] convertImageToByteArray(BufferedImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Write the BufferedImage to the ByteArrayOutputStream as a PNG image
            ImageIO.write(image, "png", outputStream);
            // Return the byte array containing the image data
            return outputStream.toByteArray();
        } catch (IOException e) {
            // Print error message if there's an exception
            System.err.println("Error converting image to byte array: " + e.getMessage());
            return null; // Return null if there's an error
        }
    }

    // Method to save the image as a PNG file
    private void saveAs() throws IOException {
        // Create a file dialog for saving the image file
        FileDialog saveAsDialog = new FileDialog(frame, "Save Image", FileDialog.SAVE);
        saveAsDialog.setVisible(true);

        // If a file is selected for saving
        if (saveAsDialog.getFile() != null) {
            // Get the directory and file name for saving
            this.savePath = saveAsDialog.getDirectory();
            this.pictureName = saveAsDialog.getFile();

            // Write the BufferedImage to a PNG file
            ImageIO.write(clientMethods.getImage(), "png", new File(savePath + pictureName));
        }
    }

    // Method to save the image using the previously specified file name and directory
    private void save() throws IOException {
        // Check if a file name has been specified
        if (pictureName == null) {
            JOptionPane.showMessageDialog(null, "Please save as first.");
        } else {
            // Write the BufferedImage to a PNG file using the specified file name and directory
            ImageIO.write(clientMethods.getImage(), "png", new File(savePath + pictureName));
        }
    }

}
