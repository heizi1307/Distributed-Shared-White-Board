import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestClient {
    public static void main(String[] args) {
        try{
            Registry registry = LocateRegistry.getRegistry("localhost", 1234);
            TestInterface dict = (TestInterface) registry.lookup("Dictionary");
        } catch (Exception e){

        }
    }
}
