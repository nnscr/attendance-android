package de.nnscr.attendance.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.nnscr.attendance.MessageCallback;
import de.nnscr.attendance.SummaryCallback;
import de.nnscr.attendance.adapter.SummaryWeekAdapter;
import de.nnscr.attendance.listener.SummaryRecordsResultListener;
import de.nnscr.attendance.listener.SummaryResponseCallback;
import de.nnscr.attendance.model.SummaryBlock;
import de.nnscr.attendance.model.SummaryTimeSpan;
import de.nnscr.attendance.model.SummaryWeek;

/**
 * Created by philipp on 16.03.15.
 */
public class SummaryManager extends AbstractManager {
    protected SharedPreferences preferences;
    protected Context context;
    protected SummaryCallback listener;

    public SummaryManager(Context context, SummaryCallback listener) {
        super(context);
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.listener = listener;
    }

    public void authenticate() {
        String user = preferences.getString("username", "");
        String pwd  = preferences.getString("password", "");

        try {
            this.sendMessage("Session:login", "{\"username\": \"" + user + "\", \"password\": \"" + pwd + "\"}", new MessageCallbackHandler() {
                @Override
                public void onResult(JSONObject result) throws JSONException{
                    if (result.getBoolean("_success")) {
                        token = result.getString("token");

                        listener.onAuthenticate();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getSummaries(final List<? extends SummaryTimeSpan> model, final ArrayAdapter adapter) {
        if (model.size() == 0) {
            return;
        }

        JSONObject blocks = new JSONObject();

        for (SummaryTimeSpan span : model) {
            JSONObject block = new JSONObject();
            try {
                block.put("start", span.getStart().toString("YYYY-MM-dd HH:mm:ss"));
                block.put("end", span.getEnd().toString("YYYY-MM-dd HH:mm:ss"));

                blocks.put(span.getIdentifier(), block);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String employee = preferences.getString("employee", "");
        String payload = "{"
                + "\"employeeId\": \"" + employee + "\","
                + "\"blocks\": " + blocks.toString()
                + "}";

        try {
            sendMessage("hrm:getSummaries", payload, new MessageCallbackHandler() {
                @Override
                public void onResult(JSONObject json) throws JSONException {
                    JSONObject totals = json.getJSONObject("totals");

                    for (SummaryTimeSpan span : model) {
                        span.setTotalAttendance(totals.getLong(span.getIdentifier()));
                        Log.i("week " + span.getIdentifier(), String.valueOf(span.getTotalAttendance()));
                    }

                    adapter.notifyDataSetChanged();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getRecords(DateTime start, DateTime end, final SummaryRecordsResultListener callback) {
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        String employee = preferences.getString("employee", "");

        String payload = "{"
            + "\"employeeId\": \"" + employee + "\","
            + "\"start\": \"" + start.toString(format) + "\","
            + "\"end\": \"" + end.toString(format) + "\""
            + "}";

        final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        try {
            sendMessage("hrm:getRecords", payload, new MessageCallbackHandler() {
                @Override
                public void onResult(JSONObject result) throws JSONException {
                    if (result.getBoolean("_success")) {
                        JSONObject jsonBlocks = result.getJSONObject("blocks");

                        ArrayList<SummaryBlock> blocks = new ArrayList<>();

                        for (int i = 0, j = jsonBlocks.length(); i < j; ++i) {
                            JSONObject jsonBlock = jsonBlocks.getJSONObject(Integer.toString(i));
                            SummaryBlock block = new SummaryBlock();

                            block.start = dateTimeFormatter.parseDateTime(jsonBlock.getString("begin"));
                            block.end = dateTimeFormatter.parseDateTime(jsonBlock.getString("end"));

                            blocks.add(block);
                        }

                        callback.onResult(blocks);
                    } else if (result.getString("_status").equals("Invalid or no token supplied.")) {
                        authenticate();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pollFirstRecord() {
        String employee = preferences.getString("employee", "");
        String payload = "{\"employeeId\":\"" + employee + "\"}";

        try {
            sendMessage("hrm:getFirstRecord", payload, new MessageCallbackHandler() {
                @Override
                public void onResult(JSONObject result) throws JSONException {
                    Log.i("first record", result.toString(2));
                    DateTimeFormatter format = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");
                    DateTime time = format.parseDateTime(result.getJSONObject("record").getString("begin"));

                    listener.onFirstResult(time);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
