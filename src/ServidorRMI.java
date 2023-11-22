import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorRMI {
/* @param args the command line arguments */
    public static void main(String[] args) { // TODO code application logic here
        try {
            Registry rmi = LocateRegistry.createRegistry(1005); //usuario
            rmi.rebind("ZipCompresser", (Remote) new ServerConnection());
            System.out.println("Servidor Activo");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}