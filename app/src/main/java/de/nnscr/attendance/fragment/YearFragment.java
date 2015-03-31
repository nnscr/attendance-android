package de.nnscr.attendance.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.nnscr.attendance.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {link YearFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {link YearFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class YearFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_year, container, false);
    }
}
