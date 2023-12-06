
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientThread extends UnicastRemoteObject implements ClientConnection, Runnable {

    ServerMethods servidor;
    List<File> files;

    public String nombre = null;
    public Ventana ventana = null;

    ClientThread(String nombre, ServerMethods servidor, Ventana ventana) throws RemoteException{
        this.nombre = nombre;
        this.servidor = servidor;
        this.ventana = ventana;
        servidor.registro(this, nombre);
    }
    @Override
    public void receiveData(byte[] file, String time, String method) throws RemoteException{
        System.out.println(file);
        System.out.println("Tiempo tardado: " + time);
        try {
            convertByteArrayToZip(file, method);
            System.out.println("Archivo ZIP creado en: " + this.ventana.fileChoserDestiny.getSelectedFile().getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        GUIThread guiThread = new GUIThread(this.ventana, time, method);
        guiThread.start();
    }

    public void convertByteArrayToZip(byte[] byteArray, String method) throws IOException {
        FileOutputStream fos = new FileOutputStream(this.ventana.fileChoserDestiny.getSelectedFile().getPath()+"/"+method+".zip");
        fos.write(byteArray);
        fos.close();
    }
    @Override
    public void run() {
        Scanner s = new Scanner(System.in);
        String mensaje;

        while(true){
            mensaje = s.nextLine();
            try{

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void sendData(ArrayList<byte[]> files, String method) throws RemoteException {
        servidor.receiveFiles(files, this.nombre, method);
    }
}