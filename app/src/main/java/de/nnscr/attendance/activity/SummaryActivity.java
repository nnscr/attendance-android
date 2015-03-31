package de.nnscr.attendance.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.nnscr.attendance.R;
import de.nnscr.attendance.SummaryCallback;
import de.nnscr.attendance.adapter.SummaryDayAdapter;
import de.nnscr.attendance.adapter.SummaryWeekAdapter;
import de.nnscr.attendance.listener.SummaryResponseCallback;
import de.nnscr.attendance.manager.AbstractManager;
import de.nnscr.attendance.manager.SummaryManager;
import de.nnscr.attendance.model.SummaryDay;
import de.nnscr.attendance.model.SummaryTimeSpan;
import de.nnscr.attendance.model.SummaryWeek;


public class SummaryActivity extends ActionBarActivity implements SummaryCallback, TabHost.OnTabChangeListener {
    protected SummaryManager manager;

    private TabHost tabHost;
    private TabHost.TabSpec tabYear;
    private TabHost.TabSpec tabWeek;
    private TabHost.TabSpec tabDay;

    private List<HashMap<String, String>> modelYears;
    private SimpleAdapter adapterYears;

    private ArrayList<SummaryWeek> modelWeeks;
    private SummaryWeekAdapter adapterWeeks;

    private ArrayList<SummaryDay> modelDays;
    private SummaryDayAdapter adapterDays;

    private int selectedYear;
    private int selectedWeek;
    private int selectedDay;

    private DateTime firstRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        this.firstRecord = new DateTime();

        this.manager = new SummaryManager(this, this);
        this.manager.pollFirstRecord();

        // default values
        DateTime now = new DateTime();
        selectedYear = now.getYear();
        selectedWeek = now.getWeekOfWeekyear();

        // setup tabs
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setOnTabChangedListener(this);
        tabHost.setup();

        // year tab
        tabYear = tabHost.newTabSpec("years");
        tabYear.setContent(R.id.tab1);
        tabYear.setIndicator(Integer.toString(now.getYear()));
        tabHost.addTab(tabYear);

        // week tab
        tabWeek = tabHost.newTabSpec("weeks");
        tabWeek.setContent(R.id.tab2);
        tabWeek.setIndicator("KW " + Integer.toString(now.getWeekOfWeekyear()));
        tabHost.addTab(tabWeek);

        // day week
        tabDay = tabHost.newTabSpec("days");
        tabDay.setContent(R.id.tab3);
        tabDay.setIndicator(now.toString("dd.MM."));
        tabHost.addTab(tabDay);

        // setup list views
        setupYearsList();
        setupWeeksList();
        setupDaysList();
    }

    private void setupYearsList() {
        String[] from = new String[] { "title", "value"};
        int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

        modelYears = new ArrayList<>();

        for (int year = DateTime.now().getYear(), firstYear = firstRecord.getYear(); year >= firstYear; --year) {
            modelYears.add(createListItem(Integer.toString(year), ""));
        }

        ListView listView = (ListView)findViewById(R.id.listViewYear);
        adapterYears = new SimpleAdapter(this, modelYears, android.R.layout.simple_expandable_list_item_1, from, to);
        listView.setAdapter(adapterYears);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> obj = (HashMap<String, String>) adapterYears.getItem(i);
                selectYear(Integer.parseInt(obj.get("title")));
            }
        });
    }

    private void setupWeeksList() {
        modelWeeks = new ArrayList<>();

        DateTime dateTime = new DateTime().withYear(selectedYear);

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
            modelWeeks.add(new SummaryWeek(selectedYear, week));
        }

        // wire up list view
        ListView listView = (ListView)findViewById(R.id.listViewWeek);
        adapterWeeks = new SummaryWeekAdapter(this, modelWeeks);
        listView.setAdapter(adapterWeeks);

        // query summaries from server
        manager.getSummaries(modelWeeks, adapterWeeks);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SummaryWeek week = adapterWeeks.getItem(i);
                selectWeek(week.getWeekNumber());
            }
        });
    }

    private void setupDaysList() {
        modelDays = new ArrayList<>();

        DateTime current = new DateTime()
                .withYear(selectedYear)
                .withWeekOfWeekyear(selectedWeek)
                .withDayOfWeek(DateTimeConstants.MONDAY);

        DateTime weekEnd = current.withDayOfWeek(DateTimeConstants.SUNDAY).plusSeconds(1);

        while (current.isBefore(weekEnd)) {
            modelDays.add(new SummaryDay(current));
            current = current.plusDays(1);
        }

        // wire up list view
        ListView listView = (ListView)findViewById(R.id.listViewDay);
        adapterDays = new SummaryDayAdapter(this, modelDays);
        listView.setAdapter(adapterDays);

        // query summaries from server
        manager.getSummaries(modelDays, adapterDays);
    }

    private void selectYear(int year) {
        String strYear = Integer.toString(year);
        selectedYear = year;

        // set tab text to selected year
        TextView tv = (TextView)tabHost.getCurrentTabView().findViewById(android.R.id.title);
        tv.setText(strYear);
        tabYear.setIndicator(strYear);

        // switch tab
        tabHost.setCurrentTabByTag("weeks");
        setupWeeksList();
    }

    private void selectWeek(int week) {
        String strWeek = "KW " + week;
        selectedWeek = week;

        // set tab text to selected week
        TextView tv = (TextView)tabHost.getCurrentTabView().findViewById(android.R.id.title);
        tv.setText(strWeek);
        tabYear.setIndicator(strWeek);

        // switch tab
        tabHost.setCurrentTabByTag("days");
        setupDaysList();
    }

    private HashMap<String, String> createListItem(String line1, String line2) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("title", line1);
        hashMap.put("value", line2);

        return hashMap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_summary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        firstRecord = start;
        setupYearsList();

        Log.i("start time", start.toString());
    }

    @Override
    public void onTabChanged(String s) {
        // Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
