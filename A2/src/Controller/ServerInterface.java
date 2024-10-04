/**
 * Author: Longxin Li
 * Student id: 1486450
 * Date: 5/16/2024
 */
package Controller;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface ServerInterface extends Remote {
    public void register(ClinetInterface client) throws RemoteException;

    public void sendMessageToServer(MessageFormat message) throws RemoteException;

    public void refresh() throws RemoteException;

    public void exitClient(String userName) throws RemoteException;

    void kickClient(String userName) throws RemoteException;

    public void removeAllClient() throws RemoteException, IOException;

    public void startChat(String text) throws RemoteException;

    public byte[] sendWhiteBoard() throws IOException, RemoteException;

    public void sendOpenedImage(byte[] rawImage) throws IOException, RemoteException;

    public Set<ClinetInterface> getClients() throws RemoteException;
}
