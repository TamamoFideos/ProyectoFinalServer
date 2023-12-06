import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ServerConnection extends UnicastRemoteObject implements ServerMethods{
    public ArrayList<ClientConnection> clientConnections;
    HashMap<String, ClientConnection> clients = new HashMap<>();

    public static TimerThread timerThread = new TimerThread();

    ForkJoinPool forkJoinPool = new ForkJoinPool();
    HashMap<String, ClientInformation> cacheInformation = new HashMap<>();
    public ServerConnection() throws RemoteException{
        clientConnections = new ArrayList<>();
        this.deleteFilesInTempDirectory();
    }

    @Override
    public void registro(ClientConnection client, String id) throws RemoteException {
        System.out.println("Cliente conectado ID: " + id);
        this.clientConnections.add(client);
        this.clients.put(id, client);
    }

    @Override
    public void mensaje(String mensaje) throws RemoteException {

    }

    @Override
    public void receiveFiles(ArrayList<byte[]> files, String id, String method) throws RemoteException {
        try {
            System.out.println("Archivos recibidos de cliente: " + id);
            ClientInformation clientInformation = new ClientInformation(method, files, id);
            this.cacheInformation.put(id, clientInformation);
            if(cacheInformation.size() > 1){
                String defaultMethod = method;
                boolean different = false;
                ArrayList<byte[]> finalFiles = new ArrayList<>();
                for (Map.Entry<String, ClientInformation> entrada : cacheInformation.entrySet()) {
                    ClientInformation valor = entrada.getValue();
                    String propiedad = valor.method;
                    if(!propiedad.equals(defaultMethod)){
                        different = true;
                    }
                    finalFiles.addAll(valor.files);
                }
                if (different){
                    for (Map.Entry<String, ClientInformation> entrada : cacheInformation.entrySet()) {
                        ClientInformation valor = entrada.getValue();
                        switch (valor.method){
                            case "Secuencial":
                                if (!ServerConnection.timerThread.contando){
                                    ServerConnection.timerThread.startCountingTime();
                                }
                                FileOutputStream fos = new FileOutputStream("secuencial-compressed.zip");
                                ZipOutputStream zipOut = new ZipOutputStream(fos);
                                ArrayList<File> filesConverted = convertByteArraysToFiles(finalFiles);
                                File[] arreglo = new File[filesConverted.size()];
                                arreglo = filesConverted.toArray(arreglo);
                                byte[] zipfile = compressSecuencial(arreglo,fos,zipOut);
                                timerThread.endCount();
                                clients.get(valor.id).receiveData(zipfile, String.valueOf(timerThread.getTime()), "Secuencial");
                                break;
                            case "ForkJoin":
                                if (!ServerConnection.timerThread.contando){
                                    ServerConnection.timerThread.startCountingTime();
                                }
                                File outputZipFile = new File("fork-join-compressed.zip");
                                if (outputZipFile.exists()){
                                    outputZipFile.delete();
                                }
                                FileOutputStream fos2;
                                ZipOutputStream zipOutputStream;
                                ArrayList<File> list  = convertByteArraysToFiles(finalFiles);
                                try {
                                    fos2 = new FileOutputStream(outputZipFile, true);
                                    zipOutputStream = new ZipOutputStream(fos2);
                                } catch (FileNotFoundException ex) {
                                    throw new RuntimeException(ex);
                                }
                                ForkJoin compressTask = new ForkJoin(list, outputZipFile, fos2, zipOutputStream);
                                forkJoinPool.invoke(compressTask);
                                try {
                                    zipOutputStream.close();
                                    fos2.close();
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                                timerThread.endCount();
                                ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                                File zipFile = new File("fork-join-compressed.zip");
                                FileInputStream newFis2 = new FileInputStream(zipFile);
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = newFis2.read(buffer)) != -1) {
                                    bos2.write(buffer, 0, bytesRead);
                                }
                                newFis2.close();
                                bos2.close();
                                files.clear();
                                clients.get(valor.id).receiveData(bos2.toByteArray(), String.valueOf(timerThread.getTime()), "ForkJoin");
                                break;
                            case "ExecutorService":
                                if (!ServerConnection.timerThread.contando){
                                    ServerConnection.timerThread.startCountingTime();
                                }
                                ArrayList<File> executorFiles = convertByteArraysToFiles(finalFiles);
                                File[] executorArreglo = new File[executorFiles.size()];
                                executorArreglo = executorFiles.toArray(executorArreglo);
                                ExecutorServiceMerge.ExecutorMerge(executorArreglo);
                                timerThread.endCount();
                                ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
                                File zipFile3 = new File("executor-compressed.zip");
                                FileInputStream newFis3 = new FileInputStream(zipFile3);
                                byte[] buffe2 = new byte[1024];
                                int bytesRead2;
                                while ((bytesRead2 = newFis3.read(buffe2)) != -1) {
                                    bos3.write(buffe2, 0, bytesRead2);
                                }
                                newFis3.close();
                                bos3.close();
                                files.clear();
                                clients.get(valor.id).receiveData(bos3.toByteArray(), String.valueOf(timerThread.getTime()), "ExecutorService");
                                break;
                            default :
                                break;
                        }
                    }
                }else{
                    switch (cacheInformation.get(id).method){
                        case "Secuencial":
                            if (!timerThread.contando){
                                timerThread.startCountingTime();
                            }
                            final FileOutputStream fos = new FileOutputStream("secuencial-compressed.zip");
                            ZipOutputStream zipOut = new ZipOutputStream(fos);
                            ArrayList<File> filesConverted = convertByteArraysToFiles(finalFiles);
                            File[] arreglo = new File[filesConverted.size()];
                            arreglo = filesConverted.toArray(arreglo);
                            byte[] zipfile = compressSecuencial(arreglo,fos,zipOut);
                            timerThread.endCount();
                            for (Map.Entry<String, ClientInformation> entrada : cacheInformation.entrySet()) {
                                ClientInformation valor = entrada.getValue();
                                clients.get(valor.id).receiveData(zipfile, String.valueOf(timerThread.getTime()), "Secuencial");
                            }
                            break;
                        case "ForkJoin":
                            if (!ServerConnection.timerThread.contando){
                                ServerConnection.timerThread.startCountingTime();
                            }
                            File outputZipFile = new File("fork-join-compressed.zip");
                            if (outputZipFile.exists()){
                                outputZipFile.delete();
                            }
                            FileOutputStream fos2;
                            ZipOutputStream zipOutputStream;
                            try {
                                fos2 = new FileOutputStream(outputZipFile, true);
                                zipOutputStream = new ZipOutputStream(fos2);
                            } catch (FileNotFoundException ex) {
                                throw new RuntimeException(ex);
                            }
                            ArrayList<File> list  = convertByteArraysToFiles(finalFiles);
                            ForkJoin compressTask = new ForkJoin(list, outputZipFile, fos2, zipOutputStream);
                            forkJoinPool.invoke(compressTask);
                            try {
                                zipOutputStream.close();
                                fos2.close();
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            timerThread.endCount();
                            ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                            File zipFile2 = new File("fork-join-compressed.zip");
                            FileInputStream newFis2 = new FileInputStream(zipFile2);
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = newFis2.read(buffer)) != -1) {
                                bos2.write(buffer, 0, bytesRead);
                            }
                            newFis2.close();
                            bos2.close();
                            files.clear();
                            for (Map.Entry<String, ClientInformation> entrada : cacheInformation.entrySet()) {
                                ClientInformation valor = entrada.getValue();
                                clients.get(valor.id).receiveData(bos2.toByteArray(), String.valueOf(timerThread.getTime()), "ForkJoin");
                            }
                            break;
                        case "ExecutorService":
                            for (Map.Entry<String, ClientInformation> entrada : cacheInformation.entrySet()) {
                                ClientInformation valor = entrada.getValue();

                                if (!ServerConnection.timerThread.contando){
                                    ServerConnection.timerThread.startCountingTime();
                                }
                                ArrayList<File> executorFiles = convertByteArraysToFiles(finalFiles);
                                File[] executorArreglo = new File[executorFiles.size()];
                                executorArreglo = executorFiles.toArray(executorArreglo);

                                ExecutorServiceMerge.ExecutorMerge(executorArreglo);
                                timerThread.endCount();
                                ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
                                File zipFile3 = new File("executor-compressed.zip");
                                FileInputStream newFis3 = new FileInputStream(zipFile3);
                                byte[] buffe2 = new byte[1024];
                                int bytesRead2;
                                while ((bytesRead2 = newFis3.read(buffe2)) != -1) {
                                    bos3.write(buffe2, 0, bytesRead2);
                                }
                                newFis3.close();
                                bos3.close();
                                files.clear();
                                for (Map.Entry<String, ClientInformation> entrada2 : cacheInformation.entrySet()) {
                                    ClientInformation valor2 = entrada.getValue();
                                    clients.get(valor2.id).receiveData(bos3.toByteArray(), String.valueOf(timerThread.getTime()), "ExecutorService");
                                }
                            }
                            break;
                        default :
                            break;
                    }
                }
                this.cacheInformation.clear();
                deleteFilesInTempDirectory();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static  byte[] compressSecuencial(File[] listOfFiles, FileOutputStream fos, ZipOutputStream zipOut) throws IOException {
        for (File srcFile : listOfFiles) {
            FileInputStream fis = new FileInputStream(srcFile);
            ZipEntry zipEntry = new ZipEntry(srcFile.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            for (int i = 0; i < bytes.length/200; i++) {
                FileOutputStream fos2 = new FileOutputStream(srcFile);
                ZipOutputStream zipOutputStream = new ZipOutputStream(fos2);
                zipOutputStream.flush();
            }
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        File zipFile = new File("secuencial-compressed.zip");
        FileInputStream newFis = new FileInputStream(zipFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = newFis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        newFis.close();
        return bos.toByteArray();
    }

    public static void addToZip(File[] files, ZipOutputStream zipOut) throws IOException {
        synchronized (zipOut) { // Exclusión mutua para evitar conflictos de escritura
            for (File file : files) {
                FileInputStream fis = new FileInputStream(file);
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOut.putNextEntry(zipEntry);
                byte[] buffer = new byte[1024];
                for (int i = 0; i < buffer.length / 500; i++) {
                    FileOutputStream fos2 = new FileOutputStream(file);
                    ZipOutputStream zipOutputStream = new ZipOutputStream(fos2);
                    zipOutputStream.flush();
                }
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, length);
                }
                fis.close();
            }
        }
    }
    public static void addToZipSingle(File file, ZipOutputStream zipOut) throws IOException {
        synchronized (zipOut) { // Exclusión mutua para evitar conflictos de escritura
            FileInputStream fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zipOut.write(buffer, 0, length);
            }
            fis.close();

        }
    }

    public ArrayList<File> convertByteArraysToFiles(ArrayList<byte[]> fileDataList) throws IOException {
        ArrayList<File> files = new ArrayList<>();
        File directory = new File("temp_files");
        if (!directory.exists()) {
            directory.mkdir(); // Crea el directorio si no existe
        }
        // Inicializar fileIndex con la cantidad de archivos en el directorio
        File[] existingFiles = directory.listFiles();
        int fileIndex = (existingFiles == null) ? 0 : existingFiles.length;
        for (byte[] fileData : fileDataList) {
            File file = new File("temp_files", "file" + (fileIndex++) + ".txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileData);
            fos.close();
            files.add(file);
        }
        return files;
    }

    public void deleteFilesInTempDirectory() {
        File directory = new File("temp_files");
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) { // Asegúrate de que no sea un directorio
                    file.delete(); // Elimina el archivo
                }
            }
        }
    }
}
