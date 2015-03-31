package de.nnscr.attendance.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import org.json.JSONException;
import java.io.IOException;
import de.nnscr.attendance.AsyncMessage;
import de.nnscr.attendance.MessageCallback;

/**
 * Created by philipp on 16.03.15.
 */
abstract public class AbstractManager {
    protected static String token;
    protected SharedPreferences preferences;
    protected Context context;

    abstract public class MessageCallbackHandler implements MessageCallback {
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

    public AbstractManager(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    protected void sendMessage(String msg, MessageCallback callback) throws IOException {
        sendMessage(msg, null, callback);
    }

    protected void sendMessage(String msg, String payload, MessageCallback callback) throws IOException {
        AsyncMessage m = new AsyncMessage(callback);

        String tok = "";

        if (AbstractManager.token != null) {
            tok = "?token=" + AbstractManager.token;
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
