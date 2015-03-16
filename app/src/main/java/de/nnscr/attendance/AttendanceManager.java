package de.nnscr.attendance;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

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
    protected Context context;

    protected class MessageCallbackHandler implements MessageCallback {
        @Override
        public void onResult(JSONObject result) throws JSONException {

        }

        @Override
        public void onException(Exception e) {
            Toast t = Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
            t.show();
        }

        @Override
        public void onMalformedJSON(String malformedJSON, JSONException exception) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("JSON Fehler");
            builder.setMessage(malformedJSON);
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public AttendanceManager(Context context, StatusChangeEventListener listener) {
        this.context = context;
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

    protected MessageCallback getStatusResultHandler() {
        return new MessageCallbackHandler() {
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

                            // recalculate the time diff for the local time to synchronize time differences
                            long diff = (now.getTime() - date.getTime());
                            Date startTime = new Date(new Date().getTime() - diff);

                            listener.onSetStartTime(startTime);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (result.has("name")) {
                        listener.onNameChange(result.getString("name"));
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

        String uri;
        if (preferences.getBoolean("dev_mode", false)) {
            uri = preferences.getString("dev_url", "");
        } else {
            uri = preferences.getString("url", "");
        }

        m.execute(uri + "/ws/" + msg + tok, payload);
    }


}
