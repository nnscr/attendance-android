package de.nnscr.attendance.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.widget.SimpleAdapter;
import android.widget.TabHost;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.nnscr.attendance.R;
import de.nnscr.attendance.SummaryCallback;
import de.nnscr.attendance.SummaryMediator;
import de.nnscr.attendance.adapter.SummaryDayAdapter;
import de.nnscr.attendance.adapter.SummaryPagerAdapter;
import de.nnscr.attendance.adapter.SummaryWeekAdapter;
import de.nnscr.attendance.listener.DrawFragmentListener;
import de.nnscr.attendance.manager.SummaryManager;
import de.nnscr.attendance.model.SummaryDay;
import de.nnscr.attendance.model.SummaryState;
import de.nnscr.attendance.model.SummaryWeek;


public class SummaryActivity extends ActionBarActivity implements SummaryCallback, DrawFragmentListener {
    protected SummaryManager manager;

    private ViewPager viewPager;
    private SummaryPagerAdapter pagerAdapter;

    private SummaryMediator mediator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        manager = new SummaryManager(this, this);
        mediator = new SummaryMediator(manager, this);

        manager.pollFirstRecord();

        pagerAdapter = new SummaryPagerAdapter(getSupportFragmentManager(), mediator);
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);

        viewPager.setOffscreenPageLimit(3);
        updateActionbar();
    }

    private void updateActionbar() {
        SummaryState state = mediator.getState();
        getSupportActionBar().setSubtitle(
                String.format("KW %02d / %4d", state.getWeek(), state.getYear())
        );
    }

    @Override
    public void onStateChanged() {
        pagerAdapter.refreshAll();
        updateActionbar();
    }

    @Override
    public void onSwitchToWeeks() {
        viewPager.setCurrentItem(SummaryPagerAdapter.ID_WEEK);
    }

    @Override
    public void onSwitchToDays() {
        viewPager.setCurrentItem(SummaryPagerAdapter.ID_DAY);
    }

    @Override
    public void onSwitchToDetails() {
        viewPager.setCurrentItem(SummaryPagerAdapter.ID_DETAIL);
    }

    @Override
    public void onAuthenticate() {

    }

    @Override
    public void onResult(JSONObject object) {
        String result;

        try {
            result = object.toString(4);
        } catch (JSONException e) {
            result = e.getMessage();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Blöcke");
        builder.setMessage(result);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onFirstResult(DateTime start) {
        mediator.setFirstRecord(start);
    }
}
