package de.nnscr.attendance;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by philipp on 27.02.15.
 */
public interface MessageCallback {
    public void onResult(JSONObject result) throws JSONException;
    public void onException(Exception e);
    public void onMalformedJSON(String malformedJSON, JSONException exception);
}
