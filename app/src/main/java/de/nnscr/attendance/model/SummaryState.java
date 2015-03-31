package de.nnscr.attendance.model;

import org.joda.time.DateTime;

/**
 * Created by philipp on 31.03.15.
 */
public class SummaryState {
    private DateTime firstRecord;
    private int year;
    private int week;

    public SummaryState() {
        // default values
        DateTime now = new DateTime();

        year = now.getYear();
        week = now.getWeekOfWeekyear();

        firstRecord = now.minusYears(2);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;

        DateTime now = new DateTime();

        if (year == now.getYear() && week > now.getWeekOfWeekyear()) {
            week = now.getWeekOfWeekyear();
        }

        if (year == firstRecord.getYear() && week < firstRecord.getWeekOfWeekyear()) {
            week = firstRecord.getWeekOfWeekyear();
        }
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public DateTime getFirstRecord() {
        return firstRecord;
    }

    public void setFirstRecord(DateTime firstRecord) {
        this.firstRecord = firstRecord;
    }

    public DateTime getCurrent() {
        return new DateTime().withYear(year).withWeekOfWeekyear(week);
    }
}
