package de.nnscr.attendance;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Attributes;

public class MainActivity extends ActionBarActivity implements StatusChangeEventListener {
    final int STATUS = 0;
    final int TOTAL = 2;
    final int BLOCK = 3;
    final int EMPLOYEE = 1;

    AttendanceManager manager;
    Notification notification;
    NotificationManager notificationManager;
    long currentTimeBlock;
    Timer timer;
    private boolean timerRunning;
    private List<HashMap<String, String>> model;
    SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // intent for clicking on the notification
        Intent resultIntent = new Intent(this, MainActivity.class);


        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Anwesend");
        builder.setContentText("Du bist momentan anwesend.");
        builder.setContentIntent(resultPendingIntent);
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
    }

    @Override
    protected void onResume() {
        manager.pollState();

        super.onResume();
    }

    private void setViewItem(int id, String value) {
        model.get(id).put("value", value);
        adapter.notifyDataSetChanged();
    }

    private void setStatusColor(int color) {
        ListView lv = (ListView)findViewById(R.id.listView);
        View item = lv.getChildAt(STATUS);
        item.setBackgroundColor(color);
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onToggle(View v) {
        manager.checkInOrOut();
    }

    @Override
    public void onStatusChange(AttendanceManager.State state) {
        Button btn = (Button)findViewById(R.id.btn_toggle);

        if (state == AttendanceManager.State.IN) {
            setViewItem(STATUS, "Anwesend");
            setStatusColor(Color.GREEN);
            btn.setText(R.string.check_out);
            showNotification();
            startTimer();
        } else {
            setViewItem(STATUS, "Abwesend");
            setStatusColor(Color.YELLOW);
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
    public void onSetStartTime(long seconds) {
        currentTimeBlock = seconds;
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
            timerRunning = true;
            timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    currentTimeBlock += 1;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
            currentTimeBlock = 0;
            timerRunning = false;
            timer.cancel();
        }
    }

    private String formatTime(long time) {
        long hours = time / 60 / 60;
        long minutes = (time - (hours * 60 * 60)) / 60;
        long seconds = time % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
