package de.nnscr.attendance;

import android.widget.ArrayAdapter;

import org.joda.time.DateTime;

import java.util.List;

import de.nnscr.attendance.listener.DrawFragmentListener;
import de.nnscr.attendance.listener.SummaryRecordsResultListener;
import de.nnscr.attendance.manager.SummaryManager;
import de.nnscr.attendance.model.SummaryDay;
import de.nnscr.attendance.model.SummaryState;
import de.nnscr.attendance.model.SummaryTimeSpan;

/**
 * Created by philipp on 31.03.15.
 */
public class SummaryMediator {
    private SummaryState state;
    private SummaryManager manager;
    private DrawFragmentListener listener;

    public SummaryMediator(SummaryManager summaryManager, DrawFragmentListener fragmentListener) {
        state = new SummaryState();
        manager = summaryManager;
        listener = fragmentListener;
    }

    public SummaryState getState() {
        return state;
    }

    public void selectYear(int year) {
        state.setYear(year);
        listener.onSwitchToWeeks();
        listener.onStateChanged();
    }

    public void selectWeek(int week) {
        state.setWeek(week);
        listener.onSwitchToDays();
        listener.onStateChanged();
    }

    public void selectDay(SummaryDay day) {
        state.setDay(day);
        listener.onSwitchToDetails();
        listener.onStateChanged();
    }

    public void setFirstRecord(DateTime firstRecord) {
        state.setFirstRecord(firstRecord);
        listener.onStateChanged();
    }

    public DateTime getFirstRecord() {
        return state.getFirstRecord();
    }

    public void requestSummaries(final List<? extends SummaryTimeSpan> model, final ArrayAdapter adapter) {
        manager.getSummaries(model, adapter);
    }

    public void requestRecords(DateTime start, DateTime end, SummaryRecordsResultListener callback) {
        manager.getRecords(start, end, callback);
    }
}
