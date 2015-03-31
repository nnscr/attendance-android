package de.nnscr.attendance.model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

/**
 * Created by philipp on 20.03.15.
 */
public class SummaryWeek extends SummaryTimeSpan {
    private int year;
    private int weekNumber;

    public SummaryWeek(int year, int week) {
        this.year = year;
        this.weekNumber = week;
        update();
    }

    private void update() {
        DateTime dt = new DateTime();
        start = dt
                .withYear(year)
                .withWeekOfWeekyear(weekNumber)
                .withDayOfWeek(DateTimeConstants.MONDAY)
                .withTime(0, 0, 0, 0);
        end = start
                .withDayOfWeek(DateTimeConstants.SUNDAY)
                .withTime(23, 59, 59, 0);

        identifier = getWeekNumberString();
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public String getWeekNumberString() {
        return Integer.toString(weekNumber);
    }

    public int getYear() {
        return year;
    }

    public String formatSpan() {
        return start.toString("dd.MM.") + " - " + end.toString("dd.MM.");
    }
}
