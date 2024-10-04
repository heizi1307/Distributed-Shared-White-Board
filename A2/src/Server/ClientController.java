/**
 * Author: Longxin Li
 * Student id: 1486450
 * Date: 5/16/2024
 */
package Server;

import Controller.ClinetInterface;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the collection of clients connected to the server.
 */
public class ClientController implements Iterable<ClinetInterface> {

    private Set<ClinetInterface> clientSet;

    /**
     * Constructs a ClientController instance.
     * @param serverMethods The server methods instance.
     */
    public ClientController(ServerMethods serverMethods){
        this.clientSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    /**
     * Adds a client to the client set.
     * @param client The client to add.
     */
    public void addClient(ClinetInterface client) {
        this.clientSet.add(client);
    }

    /**
     * Removes a client from the client set.
     * @param client The client to remove.
     */
    public void deleteClient(ClinetInterface client) {
        this.clientSet.remove(client);
    }

    /**
     * Gets the set of clients.
     * @return The set of clients.
     */
    public Set<ClinetInterface> getClientSet(){
        return this.clientSet;
    }

    /**
     * Checks if the client set is empty.
     * @return True if the client set is empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.clientSet.isEmpty();
    }

    @Override
    public Iterator<ClinetInterface> iterator() {
        return clientSet.iterator();
    }
}
