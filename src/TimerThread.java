public class TimerThread{
    public double time = 0;
    public double start = 0;
    public double end = 0;
    public boolean contando = false;
    public boolean terminoContar = false;

    public void startCountingTime(){
        contando = true;
        terminoContar = false;
        start = System.nanoTime();
    }

    public void endCount(){
        end = System.nanoTime();
        terminoContar = true;
        contando = false;
    }

    public double getTime() {
        time = (end-start)/1000000;
        return time;
    }
}
