import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipOutputStream;

public class ExecutorServiceMerge {
    public static void ExecutorMerge(File[] filesToCompress){
        int numberOfThreads = Runtime.getRuntime().availableProcessors(); // Utiliza el número de núcleos del procesador como referencia
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        try (FileOutputStream fos = new FileOutputStream("executor-compressed.zip");
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            for (File file : filesToCompress) {
                executor.submit(() -> {
                    try {
                        ServerConnection.addToZipSingle(file,zipOut);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                // Espera a que todos los hilos finalicen
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
