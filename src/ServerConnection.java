import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ServerConnection extends UnicastRemoteObject implements ServerMethods{
    public ArrayList<ClientConnection> clientConnections;

    public ServerConnection() throws RemoteException{
        clientConnections = new ArrayList<>();
    }

    @Override
    public void registro(ClientConnection client) throws RemoteException {
        System.out.println("Cliente conectado");
        this.clientConnections.add(client);
    }

    @Override
    public void mensaje(String mensaje) throws RemoteException {

    }
}
