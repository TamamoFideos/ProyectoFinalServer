import java.io.File;
import java.util.List;

public class ClientData {
    ClientConnection clientConnection;
    String id;
    List<File> files;

    public ClientData(ClientConnection clientConnection, String id, List<File> files) {
        this.clientConnection = clientConnection;
        this.id = id;
        this.files = files;
    }
}
