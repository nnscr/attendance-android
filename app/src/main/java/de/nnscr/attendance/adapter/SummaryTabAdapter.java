package de.nnscr.attendance.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.nnscr.attendance.fragment.WeekFragment;
import de.nnscr.attendance.fragment.YearFragment;

/**
 * Created by philipp on 19.03.15.
 */
public class SummaryTabAdapter extends FragmentPagerAdapter {
    public SummaryTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new YearFragment();
            case 1:
                return new WeekFragment();
            case 2:
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
