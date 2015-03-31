package de.nnscr.attendance.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.joda.time.DateTimeConstants;

import java.util.ArrayList;

import de.nnscr.attendance.R;
import de.nnscr.attendance.helper.TimeFormatHelper;
import de.nnscr.attendance.model.SummaryDay;

/**
 * Created by philipp on 20.03.15.
 */
public class SummaryDayAdapter extends ArrayAdapter<SummaryDay> {
    private static class ViewHolder {
        public TextView dayOfWeek;
        public TextView date;
        public TextView total;
    }

    public SummaryDayAdapter(Context context, ArrayList<SummaryDay> days) {
        super(context, 0, days);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SummaryDay day = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.summary_item_day, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.dayOfWeek = (TextView)convertView.findViewById(R.id.textViewDayOfWeek);
            viewHolder.date = (TextView)convertView.findViewById(R.id.textViewDate);
            viewHolder.total = (TextView)convertView.findViewById(R.id.textViewTotal);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.dayOfWeek.setText(day.getDay().dayOfWeek().getAsText());
        viewHolder.total.setText(TimeFormatHelper.formatTimeOptional(day.getTotalAttendance()));
        viewHolder.date.setText(day.getDay().toString("dd.MM.yyyy"));

        switch (day.getDay().getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
            case DateTimeConstants.SATURDAY:
                convertView.setBackgroundColor(Color.LTGRAY);
                break;

            default:
                convertView.setBackgroundColor(Color.TRANSPARENT);
                break;
        }

        return convertView;
    }
}
