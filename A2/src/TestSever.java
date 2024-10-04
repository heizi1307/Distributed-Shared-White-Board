import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

public class TestSever extends UnicastRemoteObject implements TestInterface{
    Hashtable<String, String> Dict = new Hashtable<String, String>();

    protected TestSever() throws RemoteException {
    }

    @Override
    public String Add(String word, String meaning){
        Dict.put(word, meaning);
        return "Adding " + word;
    }

    @Override
    public String Search(String word) throws RemoteException {
        return word + " means: " + Dict.get(word);
    }

    @Override
    public String Delete(String word) throws RemoteException {
        Dict.remove(word);
        return word + " removed";
    }
    public static void main(String[] args) {
        try {
            TestInterface server = new Server();
            Registry registry = LocateRegistry.createRegistry(1234);
            registry.bind("dictionary", server);
        } catch (Exception e){
        }
    }

}
