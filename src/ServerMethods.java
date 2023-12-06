import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface ServerMethods  extends Remote {
    void registro(ClientConnection cliente,String id) throws RemoteException;
    void mensaje(String mensaje) throws RemoteException;
    void receiveFiles(ArrayList<byte[]> files, String id, String method) throws RemoteException;
}
