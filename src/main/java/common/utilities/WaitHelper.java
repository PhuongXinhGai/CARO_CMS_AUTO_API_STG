package common.utilities;

public class WaitHelper {

    /**
     * Dừng luồng test trong X giây.
     *
     * @param seconds số giây cần chờ
     */
    public static void waitSeconds(int seconds) {
        try {
            System.out.println("⏳ Waiting " + seconds + "s...");
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("⚠️ Thread was interrupted while waiting");
        }
    }

    /**
     * Dừng luồng test trong X mili-giây (phù hợp khi cần delay ngắn).
     *
     * @param millis số mili-giây cần chờ
     */
    public static void waitMillis(long millis) {
        try {
            System.out.println("⏳ Waiting " + millis + "ms...");
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("⚠️ Thread was interrupted while waiting");
        }
    }
}
