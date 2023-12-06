import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.zip.ZipOutputStream;

public class ForkJoin extends RecursiveAction{
    List<File> filesToCompress;
    int izq, der;
    FileOutputStream fos;
    private File outputZipFile;
    ZipOutputStream zipOutputStream;
    public ForkJoin(List<File> A, File zipOut, FileOutputStream fos, ZipOutputStream zipOutputStream) {
        this.filesToCompress = A;
        this.outputZipFile = zipOut;
        this.fos = fos;
        this.zipOutputStream = zipOutputStream;
    }
    protected void compute() {
        if (filesToCompress.size() > 100) {
            int middle = filesToCompress.size() / 2;
            List<File> leftFiles = filesToCompress.subList(0, middle);
            List<File> rightFiles = filesToCompress.subList(middle, filesToCompress.size());
            ForkJoin leftTask = new ForkJoin(leftFiles, outputZipFile, fos,zipOutputStream);
            ForkJoin rightTask = new ForkJoin(rightFiles, outputZipFile, fos, zipOutputStream);
            invokeAll(leftTask, rightTask);
        } else {
            try {
                File[] arreglo = filesToCompress.toArray(new File[0]);
                ServerConnection.addToZip(arreglo, zipOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
