import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientConnection extends Remote {
    void receiveData(byte[] file, String time, String method) throws RemoteException;

}
