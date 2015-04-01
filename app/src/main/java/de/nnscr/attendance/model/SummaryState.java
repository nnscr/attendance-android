package de.nnscr.attendance.model;

import org.joda.time.DateTime;

/**
 * Created by philipp on 31.03.15.
 */
public class SummaryState {
    private SummaryDay summaryDay;
    private DateTime firstRecord;
    private DateTime day;
    private int year;
    private int week;

    public SummaryState() {
        // default values
        day = new DateTime();
        year = day.getYear();
        week = day.getWeekOfWeekyear();

        firstRecord = day.minusYears(2);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
        this.day = day.withYear(year);

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
        this.day = day.withWeekOfWeekyear(week);
    }

    public DateTime getFirstRecord() {
        return firstRecord;
    }

    public void setFirstRecord(DateTime firstRecord) {
        this.firstRecord = firstRecord;
    }

    public void setDay(SummaryDay day) {
        DateTime date = day.getDay();
        this.day = this.day.withDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        this.summaryDay = day;
    }

    public DateTime getCurrent() {
        return day;
    }

    public SummaryDay getSummaryDay() { return summaryDay; }
}
