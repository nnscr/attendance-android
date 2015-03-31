package de.nnscr.attendance.manager;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import de.nnscr.attendance.MessageCallback;
import de.nnscr.attendance.StatusChangeEventListener;

/**
 * Created by philipp on 27.02.15.
 */
public class AttendanceManager extends AbstractManager {
    public enum State {
        IN, OUT
    }

    public long totalTime;
    protected State state;
    protected StatusChangeEventListener listener;
    protected Context context;

    public AttendanceManager(Context context, StatusChangeEventListener listener) {
        super(context);
        this.context = context;
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


}
