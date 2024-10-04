/**
 * Author: Longxin Li
 * Student id: 1486450
 * Date: 5/16/2024
 */
package Server;

import Controller.ClinetInterface;
import Controller.MessageFormat;
import Controller.ServerInterface;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

/**
 * Implements the remote methods defined in ServerInterface.
 * Manages client registration, message forwarding, client disconnection, and other server operations.
 */
public class ServerMethods extends UnicastRemoteObject implements ServerInterface, Serializable {
    private ClientController clientController;

    /**
     * Constructs a ServerMethods instance.
     *
     * @throws RemoteException If a remote communication error occurs.
     */
    protected ServerMethods() throws RemoteException {
        this.clientController = new ClientController(this);
    }

    @Override
    public void register(ClinetInterface client) throws RemoteException {
        if (this.clientController.isEmpty()) {
            client.setManager();
        }
        boolean permitFlag = true;
        for (ClinetInterface i : this.clientController) {
            if (i.getManager()) {
                try {
                    permitFlag = i.wantToJoin(client.getUserName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (!permitFlag) {
            try {
                client.setFlag(permitFlag);
                client.closeUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            if (client.getManager()) {
                client.setUserName("(Manager)" + client.getUserName());
            }

            this.clientController.addClient(client);

            for (ClinetInterface i : this.clientController) {
                i.updateUserSet(this.clientController.getClientSet());
            }
        }

    }

    @Override
    public void sendMessageToServer(MessageFormat message) throws RemoteException {
        for (ClinetInterface i : this.clientController) {
            i.syncWhiteBoard(message);
        }
    }

    @Override
    public void refresh() throws RemoteException {
        for (ClinetInterface i : this.clientController) {
            i.refreshWhiteBoard();
        }
    }

    @Override
    public void exitClient(String userName) throws RemoteException {
        for (ClinetInterface i : this.clientController) {
            if (i.getUserName().equals(userName)) {
                this.clientController.deleteClient(i);
                System.out.println(userName + " quited.");
            }
        }
        for (ClinetInterface i : this.clientController) {
            i.updateUserSet(this.clientController.getClientSet());
        }
    }

    @Override
    public void kickClient(String userName) throws RemoteException {
        for (ClinetInterface i : this.clientController) {
            if (i.getUserName().equals(userName)) {
                this.clientController.deleteClient(i);
                System.out.println("Kicked " + userName + " out.");
                i.closeUI();
            }
        }
        for (ClinetInterface i : this.clientController) {
            i.updateUserSet(this.clientController.getClientSet());
        }
    }

    @Override
    public void removeAllClient() throws IOException {
        System.out.println("Manager closed this server.");
        for (ClinetInterface i : this.clientController) {
            this.clientController.deleteClient(i);
            i.closeUI();
        }
    }

    @Override
    public void startChat(String text) throws RemoteException {
        for (ClinetInterface i : this.clientController) {
            try {
                i.stratChat(text);
            } catch (Exception e) {
                // Handle exceptions
            }
        }
    }

    @Override
    public byte[] sendWhiteBoard() throws IOException {
        byte[] currImage = null;
        for (ClinetInterface i : this.clientController) {
            if (i.getManager()) {
                currImage = i.sendWhiteBoard();
            }
        }
        return currImage;
    }

    @Override
    public void sendOpenedImage(byte[] rawImage) throws IOException {
        for (ClinetInterface i : this.clientController) {
            if (!i.getManager()) {
                i.drawOpenedImage(rawImage);
            }
        }
    }

    @Override
    public Set<ClinetInterface> getClients() throws RemoteException {
        return this.clientController.getClientSet();
    }
}
