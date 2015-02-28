package de.nnscr.attendance;

import java.util.Date;

/**
 * Created by philipp on 27.02.15.
 */
public interface StatusChangeEventListener {
    public void onStatusChange(AttendanceManager.State state);
    public void onAuthenticate();
    public void onSetStartTime(Date startTime);
    public void onNameChange(String name);
}
