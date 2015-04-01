package de.nnscr.attendance.listener;

import java.util.ArrayList;

import de.nnscr.attendance.model.SummaryBlock;

/**
 * Created by philipp on 31.03.15.
 */
public interface SummaryRecordsResultListener {
    public void onResult(ArrayList<SummaryBlock> result);
}
