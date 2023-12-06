import java.io.FileOutputStream;
import java.io.IOException;

public class GUIThread extends  Thread{
    Ventana ventana;

    String time;
    String method;

    public GUIThread(Ventana ventana, String time, String method){
        this.ventana = ventana;
        this.time = time;
        this.method = method;
    }

    @Override
    public void run() {
        switch (method){
            case "Secuencial":
                ventana.secuencialResult(time);
                break;
            case "ExecutorService":
                ventana.executorServiceResult(time);
                break;
            case "ForkJoin":
                ventana.forkJoinResult(time);
                break;

        }
    }
}
