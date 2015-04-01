package de.nnscr.attendance.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.joda.time.DateTime;

import java.util.ArrayList;

import de.nnscr.attendance.R;
import de.nnscr.attendance.adapter.SummaryWeekAdapter;
import de.nnscr.attendance.model.SummaryWeek;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {link WeekFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {link WeekFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeekFragment extends SummaryFragment {
    private ArrayList<SummaryWeek> model;
    private SummaryWeekAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);

        model = new ArrayList<>();

        // wire up list view
        ListView listView = (ListView)view.findViewById(R.id.listView);
        adapter = new SummaryWeekAdapter(getActivity(), model);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SummaryWeek week = adapter.getItem(i);

                mediator.selectWeek(week.getWeekNumber());
            }
        });

        return view;
    }

    @Override
    public void refresh() {
        model.clear();

        DateTime dateTime = mediator.getState().getCurrent();
        int selectedYear = mediator.getState().getYear();
        DateTime firstRecord = mediator.getFirstRecord();

        // get the first and last week of the selected year
        int maxWeek = dateTime.weekOfWeekyear().getMaximumValue();
        int minWeek = dateTime.weekOfWeekyear().getMinimumValue();

        // get the current year and week
        DateTime now = new DateTime();
        int currentWeek = now.getWeekOfWeekyear();
        int currentYear = now.getYear();

        // limit maxWeek to currentWeek if the current year is selected
        if (currentYear == selectedYear && currentWeek < maxWeek) {
            maxWeek = currentWeek;
        }

        // limit minWeek to the first record
        if (firstRecord.getYear() == dateTime.getYear() && firstRecord.getWeekOfWeekyear() > minWeek) {
            minWeek = firstRecord.getWeekOfWeekyear();
        }

        // fill model (most recent week first)
        for (int week = maxWeek; week >= minWeek; --week) {
            model.add(new SummaryWeek(selectedYear, week));
        }

        // query summaries from server
        mediator.requestSummaries(model, adapter);
        adapter.notifyDataSetChanged();
    }
}
