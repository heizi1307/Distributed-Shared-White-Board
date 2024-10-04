/**
 * Author: Longxin Li
 * Student id: 1486450
 * Date: 5/16/2024
 */
package Server;

import Controller.ServerInterface;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Main class to start the server.
 */
public class Sever {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar WhiteBoardServer.jar <port>");
            System.out.println("Please input the port.");
            return;
        }

        String portArg = args[0];
        if (!portArg.matches("\\d+")) {
            System.err.println("Invalid port number: " + portArg);
            System.out.println("Please input a valid port number.");
            return;
        }

        int port = Integer.parseInt(portArg);


        try {
            // Create an instance of ServerMethods which implements ServerInterface
            ServerInterface serverInterface = new ServerMethods();

            // Create a registry on the specified port
            Registry registry = LocateRegistry.createRegistry(port);

            // Bind the server interface to the registry with the name "WhiteBoardServer"
            registry.bind("WhiteBoardServer", serverInterface);

            // Show a message dialog indicating that the server is ready to connect
            JOptionPane.showMessageDialog(null, "WhiteBoard serve is ready to connect.");
        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace();
        }
    }

}
