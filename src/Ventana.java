import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//50
//100
//500
//1000
//5000
//10000
//50000
public class Ventana extends JFrame {
    private JLabel originalArrayLabel;
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    public static ArrayList<String> files = new ArrayList<String>();

    private JLabel mergeSortTimeLabel;
    private final ExecutorService executor = Executors.newWorkStealingPool(10);
    private JLabel forkJoinTimeLabel;
    private JLabel executorServiceLabel;
    private JTextArea originalArrayTextArea;
    private JTextArea sorterdArrayTextArea;
    private JLabel sortedArrayLabel;
    final JFileChooser fileChooser = new JFileChooser();
    public final JFileChooser fileChoserDestiny = new JFileChooser();

    private JButton mergeSortButton;
    private JButton forkJoinButton;
    private JButton executorButton;
    private JButton createOriginalArrayButton;
    private JButton deleteOriginalArrayButton;
    public static TimerThread timerThread = new TimerThread();

    private JPanel leftPanel;
    private JPanel rightPanel;
    public static int[] numbers;
    public static int[] originalNumbers;

    private ClientThread clientThread;

    public Ventana(){
        try {
            Registry rmii = LocateRegistry.getRegistry("192.168.1.3", 1005);
            ServerMethods servidor = (ServerMethods) rmii.lookup("ZipCompresser");
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 10;
            Random random = new Random();
            String generatedString = random.ints(leftLimit, rightLimit + 1)
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            clientThread = new ClientThread(generatedString, servidor, this);
            new Thread(clientThread).start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        setTitle("Proyecto Final");
        setSize(1200,500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setLayout(new BorderLayout());
        createComponents();
        createListeners();
        setVisible(true);
    }
    public void createListeners(){
        mergeSortButton.addActionListener(e -> {
            if (!Ventana.timerThread.contando){
                Ventana.timerThread.startCountingTime();
            }
            try {
                File folder =fileChooser.getSelectedFile();
                File[] listOfFiles = folder.listFiles();
                ArrayList<File> list = new ArrayList<>();
                Collections.addAll(list,listOfFiles);
                clientThread.sendData(convertFilesToByteArrays(list), "Secuencial");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        forkJoinButton.addActionListener(e -> {
            File folder =fileChooser.getSelectedFile();
            File[] listOfFiles = folder.listFiles();
            ArrayList<File> list = new ArrayList<>();
            Collections.addAll(list,listOfFiles);
            try {
                clientThread.sendData(convertFilesToByteArrays(list), "ForkJoin");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        executorButton.addActionListener(e -> {
            File folder =fileChooser.getSelectedFile();
            File[] listOfFiles = folder.listFiles();
            ArrayList<File> list = new ArrayList<>();
            Collections.addAll(list,listOfFiles);
            try {
                clientThread.sendData(convertFilesToByteArrays(list), "ExecutorService");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        createOriginalArrayButton.addActionListener(e -> {
            String s = JOptionPane.showInputDialog("Ingresa el tamaño del arreglo");
            int lenght = Integer.parseInt(s);
            numbers = new int[lenght];
            for (int i = 0; i < lenght; i++) {
                numbers[i]= (int)(Math.random()*(100+1));
            }
            originalNumbers = numbers.clone();
            printFirstRectangle(originalNumbers);
        });
        deleteOriginalArrayButton.addActionListener(e -> {
            numbers = new int[]{};
            originalNumbers = numbers.clone();
            printFirstRectangle(originalNumbers);
            printSecondRectangle(originalNumbers);
        });
    }
    private void printFirstRectangle(int[] array){
        String arrStr = "";
        for (int i = 0; i < array.length; i++) {
            arrStr+=array[i]+", ";
        }
        originalArrayTextArea.setText(arrStr);
    }
    private void printSecondRectangle(int[] array){
        String arrStr = "";
        for (int i = 0; i < array.length; i++) {
            arrStr+=array[i]+", ";
        }
        sorterdArrayTextArea.setText(arrStr);
    }
    public void createComponents(){
        originalArrayLabel = new JLabel("Carpeta de Origen");
        originalArrayLabel.setFont(new Font("Verdana", Font.BOLD, 20));
        originalArrayLabel.setSize(400,150);
        originalArrayLabel.setHorizontalAlignment(JLabel.CENTER);
        originalArrayLabel.setVerticalAlignment(JLabel.CENTER);
        sortedArrayLabel = new JLabel("Carpeta destino");
        sortedArrayLabel.setFont(new Font("Verdana", Font.BOLD, 20));



        mergeSortTimeLabel = new JLabel("0.0 mS");
        mergeSortTimeLabel.setHorizontalAlignment(JLabel.CENTER);

        forkJoinTimeLabel = new JLabel("0.0 mS");
        forkJoinTimeLabel.setHorizontalAlignment(JLabel.CENTER);

        executorServiceLabel = new JLabel("0.0 mS");
        executorServiceLabel.setHorizontalAlignment(JLabel.CENTER);

        sortedArrayLabel.setSize(400,150);
        sortedArrayLabel.setHorizontalAlignment(JLabel.CENTER);
        sortedArrayLabel.setVerticalAlignment(JLabel.CENTER);
        createOriginalArrayButton = new JButton("Crear arreglo");
        deleteOriginalArrayButton = new JButton("Eliminar arreglo");
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(getWidth()-200,getHeight()));
        originalArrayTextArea = new JTextArea();
        originalArrayTextArea.setPreferredSize(new Dimension(leftPanel.getWidth(), 200));
        originalArrayTextArea.setMaximumSize(new Dimension(leftPanel.getWidth(), 450));
        originalArrayTextArea.setLineWrap(true);
        sorterdArrayTextArea = new JTextArea();
        sorterdArrayTextArea.setPreferredSize(new Dimension(leftPanel.getWidth(), 200));
        sorterdArrayTextArea.setMaximumSize(new Dimension(leftPanel.getWidth(), 450));

        sorterdArrayTextArea.setLineWrap(true);

        mergeSortButton = new JButton("Compresion Secuencial");
        mergeSortButton.setFont(new Font("Skia", Font.PLAIN, 20));
        forkJoinButton = new JButton("Fork Join");
        forkJoinButton.setFont(new Font("Skia", Font.PLAIN, 20));
        executorButton = new JButton("Executor Join");
        executorButton.setFont(new Font("Skia", Font.PLAIN, 20));
        fileChooser.setCurrentDirectory(new File("C:\\Users\\Adrian Llanos\\Desktop\\Escuela\\Archivos-proyecto-paralela"));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        fileChoserDestiny.setCurrentDirectory(new File("C:\\Users\\Adrian Llanos\\Desktop\\Escuela"));
        fileChoserDestiny.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JPanel arrayUnsortedPanel = new JPanel();
        arrayUnsortedPanel.setLayout(new BorderLayout());
        arrayUnsortedPanel.add(originalArrayLabel, BorderLayout.NORTH);
        arrayUnsortedPanel.add(fileChooser, BorderLayout.CENTER);

        JPanel arraySortedPanel = new JPanel();
        arraySortedPanel.setLayout(new BorderLayout());
        arraySortedPanel.add(sortedArrayLabel, BorderLayout.NORTH);
        arraySortedPanel.add(fileChoserDestiny, BorderLayout.CENTER);

        JPanel buttonsPannel = new JPanel();
        buttonsPannel.setLayout(new GridLayout(2,3));
        buttonsPannel.add(mergeSortTimeLabel);
        buttonsPannel.add(forkJoinTimeLabel);
        buttonsPannel.add(executorServiceLabel);

        buttonsPannel.add(mergeSortButton);
        buttonsPannel.add(forkJoinButton);
        buttonsPannel.add(executorButton);

        leftPanel.add(arrayUnsortedPanel, BorderLayout.NORTH);
        leftPanel.add(arraySortedPanel, BorderLayout.CENTER);
        leftPanel.add(buttonsPannel, BorderLayout.SOUTH);
        JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel container=new JPanel();
        container.setLayout(new BorderLayout());
        scroll.setViewportView(container);

        container.add(leftPanel, BorderLayout.CENTER);
        add(scroll);
    }



    public static void addToZip(File[] files, ZipOutputStream zipOut) throws IOException {
        synchronized (zipOut) { // Exclusión mutua para evitar conflictos de escritura
            for (File file: files) {
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

    public void secuencialResult(String time){
        this.mergeSortTimeLabel.setText(time+ " mS");
    }
    public void forkJoinResult(String time){
        this.forkJoinTimeLabel.setText(time + " mS");
    }

    public void executorServiceResult(String time){
        this.executorServiceLabel.setText(time+ " mS");
    }

    public ArrayList<byte[]> convertFilesToByteArrays(ArrayList<File> files) throws IOException {
        ArrayList<byte[]> fileDataList = new ArrayList<>();
        for (File file : files) {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }

            fileDataList.add(bos.toByteArray());

            fis.close();
            bos.close();
        }

        return fileDataList;
    }


}
