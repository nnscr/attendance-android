package de.nnscr.attendance.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.nnscr.attendance.R;
import de.nnscr.attendance.StatusChangeEventListener;
import de.nnscr.attendance.helper.TimeFormatHelper;
import de.nnscr.attendance.manager.AttendanceManager;
import de.nnscr.attendance.widget.SimpleCard;

/**
 * Created by philipp on 28.07.15.
 */
public class MainActivity extends Activity implements StatusChangeEventListener {
    private SimpleCard cardStatus;
    private SimpleCard cardEmployee;
    private SimpleCard cardBlock;
    private SimpleCard cardToday;

    private AttendanceManager manager;
    private Notification notification;
    private NotificationManager notificationManager;
    private SharedPreferences preferences;
    private Date currentBlockStart;

    private Timer timer;
    private boolean timerRunning;

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
        builder.setSmallIcon(R.drawable.ic_av_timer_black_24dp);
        builder.setContentTitle("Anwesend");
        builder.setContentText("Du bist momentan anwesend.");
        builder.setContentIntent(resultPendingIntent);
        builder.setOngoing(true);
        builder.setLights(Color.MAGENTA, 1000, 0);
        notification = builder.build();

        // build the manager
        manager = new AttendanceManager(this, this);
        manager.authenticate();

        // setup cards that display all information
        cardStatus = (SimpleCard)findViewById(R.id.card_status);
        cardStatus.setCaption("Status");
        cardStatus.setValue("Unbekannt");
        cardStatus.makeClickable(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.checkInOrOut();
            }
        });

        cardEmployee = (SimpleCard)findViewById(R.id.card_employee);
        cardEmployee.setCaption("Mitarbeiter");
        cardEmployee.setValue("Unbekannt");
        cardEmployee.makeClickable(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });

        cardBlock = (SimpleCard)findViewById(R.id.card_block);
        cardBlock.setCaption("Aktueller Block");
        cardBlock.setValue("00:00:00");

        cardToday = (SimpleCard)findViewById(R.id.card_today);
        cardToday.setCaption("Heute");
        cardToday.setValue("00:00:00");
        cardToday.makeClickable(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSummary();
            }
        });

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        manager.pollState();

        super.onResume();
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

    @Override
    public void onStatusChange(AttendanceManager.State state) {
        if (state == AttendanceManager.State.IN) {
            cardStatus.setValue("Anwesend");
            cardStatus.setValueColor(Color.GREEN);

            showNotification();
            startTimer();
        } else {
            cardStatus.setValue("Abwesend");
            cardStatus.setValueColor(Color.RED);

            hideNotification();
            stopTimer();
        }

        cardToday.setValue(formatTime(manager.totalTime));
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
        cardEmployee.setValue(name);
    }

    protected void showNotification() {
        notificationManager.notify(1, notification);
    }

    protected void hideNotification() {
        notificationManager.cancel(1);
    }

    protected void startTimer() {
        cardBlock.setValue(formatTime(0));

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

                            cardToday.setValue(formatTime(manager.totalTime + currentTimeBlock));
                            cardBlock.setValue(formatTime(currentTimeBlock));
                        }
                    });
                }
            }, 1000, 1000);
        }
    }

    protected void stopTimer() {
        cardBlock.setValue(formatTime(0));

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
