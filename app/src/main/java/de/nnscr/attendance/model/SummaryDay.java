package de.nnscr.attendance.model;

import org.joda.time.DateTime;

/**
 * Created by philipp on 20.03.15.
 */
public class SummaryDay extends SummaryTimeSpan {
    private DateTime day;

    public SummaryDay(DateTime day) {
        this.day = day;
        this.identifier = day.toString("MM-dd");
        this.start = day.withTime(0, 0, 0, 0);
        this.end = day.withTime(23, 59, 59, 0);
    }

    public DateTime getDay() {
        return this.day;
    }
}
