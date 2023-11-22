import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerMethods  extends Remote {
    void registro(ClientConnection cliente) throws RemoteException;
    void mensaje(String mensaje) throws RemoteException;
}
