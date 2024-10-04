/**
 * Author: Longxin Li
 * Student id: 1486450
 * Date: 5/16/2024
 */
package Client;

import Controller.ClinetInterface;
import Controller.ServerInterface;

import javax.swing.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.util.Random;

public class Connector {

    // Build the server address string
    public static String buildServerAddress(String hostName, String portName, String serverName) {
        return "//" + hostName + ":" + portName + "/" + serverName;
    }

    // Look up the server interface
    public static ServerInterface lookupServerInterface(String serverAddress) throws RemoteException, MalformedURLException, NotBoundException {
        return (ServerInterface) Naming.lookup(serverAddress);
    }

    // Login and register with the server
    public static void loginAndRegister(ClinetInterface clientInterface, ServerInterface serverInterface) throws RemoteException {
        login(clientInterface, serverInterface);
        if (clientInterface.getPermit()) {
            try {
                serverInterface.register(clientInterface);
            } catch (Exception e) {
                // Handle registration exceptions
            }
        } else {
            System.exit(0);
        }
    }

    // Handle user login
    private static void login(ClinetInterface clinetInterface, ServerInterface serverInterface) throws RemoteException {
        String inputUsername;
        do {
            inputUsername = JOptionPane.showInputDialog("Enter your username:");
            if (inputUsername == null) {
                // User canceled login
                JOptionPane.showMessageDialog(null, "Login canceled.");
                System.exit(0);
            }
            if (inputUsername.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username cannot be empty.");
            } else if (ifExist(inputUsername, serverInterface) || ifExist("(Manager)" + inputUsername, serverInterface)) {
                String newUsername = randomID(inputUsername);
                int option = JOptionPane.showConfirmDialog(null, "The username already exists. Your new username will be '" + newUsername + "'. Do you want to continue?", "Username Exists", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    clinetInterface.setUserName(newUsername);
                    return;
                }
            } else {
                clinetInterface.setUserName(inputUsername);
                return;
            }
        } while (true);
    }

    // Check if a username already exists
    private static boolean ifExist(String name, ServerInterface serverInterface) throws RemoteException {
        for (ClinetInterface i : serverInterface.getClients()) {
            if (name.equals(i.getUserName()) || name.equals("(Manager)" + i.getUserName())) {
                return true;
            }
        }
        return false;
    }

    // Generate a random ID for the username
    private static String randomID(String name) {
        Random random = new Random();
        int randomDigits = random.nextInt(900) + 100;
        return name + randomDigits;
    }
}
