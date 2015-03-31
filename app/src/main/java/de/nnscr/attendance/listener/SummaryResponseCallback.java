package de.nnscr.attendance.listener;

import android.support.v4.util.ArrayMap;

/**
 * Created by philipp on 29.03.15.
 */
public interface SummaryResponseCallback {
    public void onResult(ArrayMap<String, Long> summaries);
}
