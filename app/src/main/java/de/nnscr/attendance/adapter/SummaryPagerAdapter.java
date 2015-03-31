package de.nnscr.attendance.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import de.nnscr.attendance.SummaryMediator;
import de.nnscr.attendance.fragment.DayFragment;
import de.nnscr.attendance.fragment.SummaryFragment;
import de.nnscr.attendance.fragment.WeekFragment;
import de.nnscr.attendance.fragment.YearFragment;
import de.nnscr.attendance.listener.SummarySelectionListener;
import de.nnscr.attendance.model.SummaryState;

/**
 * Created by philipp on 31.03.15.
 */
public class SummaryPagerAdapter extends FragmentPagerAdapter {
    public final static int ID_YEAR = 0;
    public final static int ID_WEEK = 1;
    public final static int ID_DAY = 2;
    public final static int ID_DETAIL = 3;

    private SummaryFragment[] fragments;

    public SummaryPagerAdapter(FragmentManager fm, SummaryMediator mediator) {
        super(fm);

        fragments = new SummaryFragment[] {
            new YearFragment(),
            new WeekFragment(),
            new DayFragment(),
        };

        for (SummaryFragment fragment : fragments) {
            fragment.setMediator(mediator);
        }
    }

    public void refresh(int i) {
        fragments[i].refresh();
    }

    public void refreshAll() {
        for (int i = 0; i < getCount(); ++i) {
            refresh(i);
        }
    }

    @Override
    public Fragment getItem(int i) {
        return fragments[i];
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case ID_YEAR: return "Jahre";
            case ID_WEEK: return "Wochen";
            case ID_DAY: return "Tage";
        }

        return null;
    }
}
