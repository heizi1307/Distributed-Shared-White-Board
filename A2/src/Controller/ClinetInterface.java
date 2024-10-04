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

public interface ClinetInterface extends Remote {
    void setManager() throws RemoteException;

    boolean wantToJoin(String name) throws RemoteException;

    void setFlag(boolean flag) throws RemoteException;

    boolean getPermit() throws RemoteException;

    boolean getManager() throws RemoteException;

    String getUserName() throws RemoteException;

    void setUserName(String string) throws RemoteException;

    void updateUserSet(Set<ClinetInterface> clientSet) throws RemoteException;

    void syncWhiteBoard(MessageFormat message) throws RemoteException;

    void refreshWhiteBoard() throws RemoteException;

    void closeUI() throws RemoteException;

    void stratChat(String text) throws RemoteException;

    public byte[] sendWhiteBoard() throws IOException;

    public void drawOpenedImage(byte[] rawImage) throws IOException;

    public void whiteBoardUI(ServerInterface serverInterface) throws RemoteException;

}
