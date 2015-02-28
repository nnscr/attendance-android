package de.nnscr.attendance;

/**
 * Created by philipp on 27.02.15.
 */
public interface StatusChangeEventListener {
    public void onStatusChange(AttendanceManager.State state);
    public void onAuthenticate();
    public void onSetStartTime(long seconds);
    public void onNameChange(String name);
}
