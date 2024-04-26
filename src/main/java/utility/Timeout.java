package utility;

public class Timeout {
    public static void executeWithTimeout(Runnable runnable, long millis) {
        Thread thread = new Thread(runnable);
        thread.start();

        try {
            thread.join(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (thread.isAlive()) {
            thread.interrupt();
            throw new RuntimeException("Operation timed out");
        }
    }
}