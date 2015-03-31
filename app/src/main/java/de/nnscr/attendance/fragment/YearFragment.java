package de.nnscr.attendance.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.joda.time.DateTime;

import java.util.ArrayList;

import de.nnscr.attendance.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {link YearFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {link YearFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class YearFragment extends SummaryFragment {
    private ArrayList<Integer> model;
    private ArrayAdapter<Integer> adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_year, container, false);

        model = new ArrayList<>();

        ListView listView = (ListView)view.findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_expandable_list_item_1, model);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int item = adapter.getItem(i);

                mediator.selectYear(item);
            }
        });

        return view;
    }

    @Override
    public void refresh() {
        model.clear();

        int firstYear = new DateTime().getYear();
        int lastYear = mediator.getFirstRecord().getYear();

        for (int year = firstYear; year >= lastYear; --year) {
            model.add(year);
        }

        adapter.notifyDataSetChanged();
    }
}
