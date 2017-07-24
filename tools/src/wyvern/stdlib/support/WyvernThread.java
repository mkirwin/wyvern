package wyvern.stdlib.support;

/**
 * Created by mkirwin on 7/24/17.
 */
public class WyvernThread extends Thread {
    // Importable object
    public static WyvernThread wyvernThread;

    public void run() {
        System.out.println("wyvernThread.run() produced this sentence");
    }
}
