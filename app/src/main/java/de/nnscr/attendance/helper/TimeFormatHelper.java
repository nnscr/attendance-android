package de.nnscr.attendance.helper;

/**
 * Created by philipp on 20.03.15.
 */
public class TimeFormatHelper {
    public static String formatTime(long time) {
        long hours = time / 60 / 60;
        long minutes = (time - (hours * 60 * 60)) / 60;
        long seconds = time % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String formatTimeOptional(long time) {
        return time == 0 ? "" : formatTime(time);
    }
}
