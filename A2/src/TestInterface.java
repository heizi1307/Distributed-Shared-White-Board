import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TestInterface extends Remote {
    String Add(String word, String meaning) throws RemoteException;
    String Search(String word) throws RemoteException;
    String Delete(String word) throws RemoteException;

}
