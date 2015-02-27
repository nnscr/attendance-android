package de.nnscr.attendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by philipp on 27.02.15.
 */
public class AttendanceManager {
    public enum State {
        IN, OUT
    }

    protected long totalTime;
    protected String token;
    protected State state;
    protected SharedPreferences preferences;
    protected StatusChangeEventListener listener;

    public AttendanceManager(Context context, StatusChangeEventListener listener) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.listener = listener;
    }

    public boolean isAuthenticated() {
        return token != null;
    }

    public void authenticate() {
        String user = preferences.getString("username", "");
        String pwd  = preferences.getString("password", "");

        try {
            this.sendMessage("Session:login", "{\"username\": \"" + user + "\", \"password\": \"" + pwd + "\"}", new MessageCallback() {
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

    protected MessageCallback getStatusResultHandler() {
        return new MessageCallback() {
            @Override
            public void onResult(JSONObject result) throws JSONException {
                if (result.getBoolean("_success")) {
                    String strState = result.getString("status");

                    if (strState.equals("in")) {
                        state = State.IN;
                    } else if (strState.equals("out")) {
                        state = State.OUT;
                    }

                    totalTime = result.getLong("time");

                    if (result.has("start")) {
                        String strStart = result.getString("start");
                        String strNow   = result.getString("now");
                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                        try {
                            Date now  = format.parse(strNow);
                            Date date = format.parse(strStart);

                            listener.onSetStartTime((now.getTime() - date.getTime()) / 1000);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    listener.onStatusChange(state);
                } else if (result.getString("_status").equals("Invalid or no token supplied.")) {
                    authenticate();
                }
            }
        };
    }

    public void pollState() {
        try {
            sendMessage("hrm:getStatus", getPayload(), getStatusResultHandler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkInOrOut() {
        try {
            sendMessage("hrm:checkInOrOut", getPayload(), getStatusResultHandler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPayload() {
        String employee = preferences.getString("employee", "");

        return "{\"employeeId\": \"" + employee + "\"}";
    }

    protected void sendMessage(String msg, MessageCallback callback) throws IOException {
        sendMessage(msg, null, callback);
    }

    protected void sendMessage(String msg, String payload, MessageCallback callback) throws IOException {
        AsyncMessage m = new AsyncMessage(callback);

        String tok = "";

        if (this.token != null) {
            tok = "?token=" + this.token;
        }

        m.execute(preferences.getString("url", "") + "/ws/" + msg + tok, payload);
    }


}
