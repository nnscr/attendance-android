package de.nnscr.attendance.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.joda.time.DateTime;

import java.util.ArrayList;

import de.nnscr.attendance.R;
import de.nnscr.attendance.adapter.SummaryBlockAdapter;
import de.nnscr.attendance.adapter.SummaryDayAdapter;
import de.nnscr.attendance.listener.SummaryRecordsResultListener;
import de.nnscr.attendance.model.SummaryBlock;
import de.nnscr.attendance.model.SummaryBlockHeader;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * { DetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends SummaryFragment {
    ArrayList<SummaryBlock> model;
    SummaryBlockAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        model = new ArrayList<>();

        // wire up list view
        ListView listView = (ListView)view.findViewById(R.id.listView);
        adapter = new SummaryBlockAdapter(getActivity(), model);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void refresh() {
        DateTime current = mediator.getState().getCurrent();
        DateTime start = current.withTime(0, 0, 0, 0);
        DateTime end = current.withTime(23, 59, 59, 0);

        model.clear();

        SummaryBlockHeader header = new SummaryBlockHeader();
        header.day = mediator.getState().getSummaryDay();

        model.add(header);

        mediator.requestRecords(start, end, new SummaryRecordsResultListener() {
            @Override
            public void onResult(ArrayList<SummaryBlock> result) {
                model.addAll(result);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
