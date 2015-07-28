package de.nnscr.attendance.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.nnscr.attendance.helper.TimeFormatHelper;
import de.nnscr.attendance.manager.AttendanceManager;
import de.nnscr.attendance.R;
import de.nnscr.attendance.StatusChangeEventListener;

public class MainActivity extends Activity implements StatusChangeEventListener {
    final int STATUS = 0;
    final int TOTAL = 2;
    final int BLOCK = 3;
    final int EMPLOYEE = 1;
    final int DEVELOPER = 4;

    AttendanceManager manager;
    Notification notification;
    NotificationManager notificationManager;
    Date currentBlockStart;
    Timer timer;
    private boolean timerRunning;
    private List<HashMap<String, String>> model;
    SimpleAdapter adapter;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // intent for clicking on the notification
        Intent resultIntent = new Intent(this, MainActivity.class);


        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // build the notification
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Anwesend");
        builder.setContentText("Du bist momentan anwesend.");
        builder.setContentIntent(resultPendingIntent);
        builder.setOngoing(true);
        builder.setLights(Color.MAGENTA, 1000, 0);
        notification = builder.build();

        // build the manager
        manager = new AttendanceManager(this, this);
        manager.authenticate();

        // item mapping
        String[] from = new String[] { "title", "value"};
        int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

        model = new ArrayList<>();
        model.add(createHashmap("Status", "Unbekannt"));
        model.add(createHashmap("Mitarbeiter", "Unbekannt"));
        model.add(createHashmap("Gesamt Heute", "00:00:00"));
        model.add(createHashmap("Aktueller Block", "00:00:00"));

        // set adapter
        ListView lv = (ListView)findViewById(R.id.listView);
        adapter = new SimpleAdapter(this, model, android.R.layout.simple_list_item_2, from, to);
        lv.setAdapter(adapter);

        // required to build the alert
        final Context context = this;

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == DEVELOPER) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setTitle("Entwicklermodus beenden?");
                    dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            preferences.edit().putBoolean("dev_mode", false).commit();
                            setDeveloperUI();
                            manager.pollState();
                        }
                    });
                    dialogBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    dialogBuilder.create().show();
                }
            }
        });

        setDeveloperUI();
    }

    @Override
    protected void onResume() {
        manager.pollState();
        setDeveloperUI();

        super.onResume();
    }

    private void setViewItem(int id, String value) {
        model.get(id).put("value", value);
        adapter.notifyDataSetChanged();
    }

    private void setStatusColor(int color) {
        ListView lv = (ListView)findViewById(R.id.listView);
        View item = lv.getChildAt(STATUS);
        //item.setBackgroundColor(color);
        TextView tv = (TextView)item.findViewById(android.R.id.text2);
        tv.setTextColor(color);
    }

    private void setDeveloperUI() {
        setDeveloperUI(preferences.getBoolean("dev_mode", false));
    }

    private void setDeveloperUI(boolean mode) {
        if (mode && model.size() <= DEVELOPER) {
            model.add(createHashmap("Entwickler", "Entwicklermodus aktiviert."));
        } else if(!mode && model.size() > DEVELOPER) {
            model.remove(DEVELOPER);
        }

        adapter.notifyDataSetChanged();
    }

    private HashMap<String, String> createHashmap(String title, String value) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("title", title);
        hashMap.put("value", value);

        return hashMap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            openSettings();
            return true;
        } else if (id == R.id.action_poll) {
            manager.pollState();
            return true;
        } else if (id == R.id.action_summary) {
            openSummary();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openSummary() {
        Intent intent = new Intent(this, SummaryActivity.class);
        startActivity(intent);
    }

    public void onToggle(View v) {
        findViewById(R.id.btn_toggle).setEnabled(false);
        manager.checkInOrOut();
    }

    @Override
    public void onStatusChange(AttendanceManager.State state) {
        Button btn = (Button)findViewById(R.id.btn_toggle);
        btn.setEnabled(true);

        if (state == AttendanceManager.State.IN) {
            setViewItem(STATUS, "Anwesend");
            setStatusColor(Color.GREEN);
            btn.setText(R.string.check_out);
            showNotification();
            startTimer();
        } else {
            setViewItem(STATUS, "Abwesend");
            setStatusColor(Color.RED);
            btn.setText(R.string.check_in);
            hideNotification();
            stopTimer();
        }

        setViewItem(TOTAL, formatTime(manager.totalTime));
    }

    @Override
    public void onAuthenticate() {
        manager.pollState();
    }

    @Override
    public void onSetStartTime(Date startTime) {
        currentBlockStart = startTime;
    }

    @Override
    public void onNameChange(String name) {
        setViewItem(EMPLOYEE, name);
    }

    protected void showNotification() {
        notificationManager.notify(1, notification);
    }

    protected void hideNotification() {
        notificationManager.cancel(1);
    }

    protected void startTimer() {
        setViewItem(BLOCK, formatTime(0));

        if (!timerRunning) {
            if (null == currentBlockStart) {
                currentBlockStart = new Date();
            }

            timerRunning = true;
            timer = new Timer(false);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long currentTimeBlock = (new Date().getTime() - currentBlockStart.getTime()) / 1000;

                            setViewItem(BLOCK, formatTime(currentTimeBlock));
                            setViewItem(TOTAL, formatTime(manager.totalTime + currentTimeBlock));
                        }
                    });
                }
            }, 1000, 1000);
        }
    }

    protected void stopTimer() {
        setViewItem(BLOCK, formatTime(0));

        if (timer != null) {
            currentBlockStart = null;
            timerRunning = false;
            timer.cancel();
        }
    }

    private String formatTime(long time) {
        return TimeFormatHelper.formatTime(time);
    }
}
