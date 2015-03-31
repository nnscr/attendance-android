package de.nnscr.attendance.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.ArrayList;

import de.nnscr.attendance.R;
import de.nnscr.attendance.adapter.SummaryDayAdapter;
import de.nnscr.attendance.model.SummaryDay;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {link WeekFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {link WeekFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DayFragment extends SummaryFragment {
    private ArrayList<SummaryDay> model;
    private SummaryDayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day, container, false);

        model = new ArrayList<>();

        // wire up list view
        ListView listView = (ListView)view.findViewById(R.id.listView);
        adapter = new SummaryDayAdapter(getActivity(), model);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void refresh() {
        if (model == null) {
            return;
        }

        model.clear();

        DateTime current = mediator.getState().getCurrent()
                .withDayOfWeek(DateTimeConstants.MONDAY);

        DateTime weekEnd = current.withDayOfWeek(DateTimeConstants.SUNDAY).plusSeconds(1);

        while (current.isBefore(weekEnd)) {
            model.add(new SummaryDay(current));
            current = current.plusDays(1);
        }

        // query summaries from server
        mediator.requestSummaries(model, adapter);
        adapter.notifyDataSetChanged();
    }
}
