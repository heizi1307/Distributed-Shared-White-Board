/**
 * Author: Longxin Li
 * Student id: 1486450
 * Date: 5/16/2024
 */
package Client;

import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MessageFormat extends UnicastRemoteObject implements Controller.MessageFormat {
    private String status;
    private String userName;
    private String mode;
    private Color color;
    private Point point;
    private String text;
    private Float weight;
    protected MessageFormat(String status, String userName, String mode, Color color, Point point, String text, Float weight) throws RemoteException {
        this.color = color;
        this.mode = mode;
        this.point = point;
        this.text = text;
        this.status = status;
        this.userName = userName;
        this.weight = weight;
    }

    @Override
    public Color getColor() throws RemoteException {
        return this.color;
    }

    @Override
    public String getText() throws RemoteException {
        return this.text;
    }

    @Override
    public Point getPoint() throws RemoteException {
        return this.point;
    }

    @Override
    public String getMode() throws RemoteException {
        return this.mode;
    }

    @Override
    public String getUserName() throws RemoteException {
        return this.userName;
    }

    @Override
    public String getStatus() throws RemoteException {
        return this.status;
    }

    @Override
    public Float getWeight() throws RemoteException {
        return this.weight;
    }

}
