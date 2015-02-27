package de.nnscr.attendance;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by philipp on 27.02.15.
 */
public interface MessageCallback {
    public void onResult(JSONObject result) throws JSONException;
}
