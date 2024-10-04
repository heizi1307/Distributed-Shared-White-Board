/**
 * Author: Longxin Li
 * Student id: 1486450
 * Date: 5/16/2024
 */
package Controller;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageFormat extends Remote {
    public Color getColor() throws RemoteException;
    public String getText() throws RemoteException;
    public Point getPoint() throws RemoteException;
    public String getMode() throws RemoteException;
    public String getUserName() throws RemoteException;
    public String getStatus() throws RemoteException;
    public Float getWeight() throws RemoteException;
}
