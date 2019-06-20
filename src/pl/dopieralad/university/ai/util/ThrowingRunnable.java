package pl.dopieralad.university.ai.util;

public interface ThrowingRunnable {

    static Runnable withException(ThrowingRunnable throwingRunnable) {
        return () -> {
            try {
                throwingRunnable.run();
            } catch (Exception exception) {
                throwingRunnable.handleException(exception);
            }
        };
    }

    void run() throws Exception;

    default void handleException(Exception exception) {
        throw new RuntimeException("An exception has occurred in throwing runnable!", exception);
    }
}
