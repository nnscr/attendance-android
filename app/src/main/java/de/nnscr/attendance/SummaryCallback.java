package de.nnscr.attendance;

import org.joda.time.DateTime;
import org.json.JSONObject;

/**
 * Created by philipp on 16.03.15.
 */
public interface SummaryCallback {
    public void onAuthenticate();
    public void onResult(JSONObject object);
    public void onFirstResult(DateTime start);
}
