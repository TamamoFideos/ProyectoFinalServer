import java.io.File;
import java.util.ArrayList;

public class ClientInformation {
    public String method;
    public ArrayList<byte[]> files;
    public String id;

    public ClientInformation(String method, ArrayList<byte[]> files, String id) {
        this.method = method;
        this.files = files;
        this.id = id;
    }
}
