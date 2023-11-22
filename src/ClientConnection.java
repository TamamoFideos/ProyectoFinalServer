import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientConnection extends Remote {
    void receiveData(String mensaje) throws RemoteException;

}
