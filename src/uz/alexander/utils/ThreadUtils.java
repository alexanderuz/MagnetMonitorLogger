package uz.alexander.utils;

public class ThreadUtils {

    public static void Sleep(long sec) {
        try {
            Thread.sleep(sec*1000);
        } catch (InterruptedException e) {
            Logger.handleException(e);
        }
    }
}
