package de.nnscr.attendance.model;

import org.joda.time.DateTime;

/**
 * Created by philipp on 16.03.15.
 */
public class SummaryTimeSpan {
    protected DateTime start;
    protected DateTime end;
    protected long totalAttendance;
    protected String identifier;

    public SummaryTimeSpan(DateTime start, DateTime end) {
        this.start = start;
        this.end = end;
    }

    public SummaryTimeSpan() {}

    public DateTime getStart() {
        return start;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public long getTotalAttendance() {
        return totalAttendance;
    }

    public void setTotalAttendance(long total) {
        totalAttendance = total;
    }

    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getIdentifier() { return identifier; }
}
