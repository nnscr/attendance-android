package de.nnscr.attendance.fragment;

import android.support.v4.app.Fragment;

import de.nnscr.attendance.SummaryMediator;
import de.nnscr.attendance.listener.SummarySelectionListener;
import de.nnscr.attendance.model.SummaryState;

/**
 * Created by philipp on 31.03.15.
 */
abstract public class SummaryFragment extends Fragment {
    protected SummaryMediator mediator;

    public void setMediator(SummaryMediator summaryMediator) {
        mediator = summaryMediator;
    }

    abstract public void refresh();
}
