package de.nnscr.attendance.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.nnscr.attendance.R;
import de.nnscr.attendance.helper.TimeFormatHelper;
import de.nnscr.attendance.model.SummaryWeek;

/**
 * Created by philipp on 20.03.15.
 */
public class SummaryWeekAdapter extends ArrayAdapter<SummaryWeek> {
    private static class ViewHolder {
        public TextView title;
        public TextView span;
        public TextView total;
    }

    public SummaryWeekAdapter(Context context, ArrayList<SummaryWeek> weeks) {
        super(context, 0, weeks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SummaryWeek week = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.summary_item_week, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView)convertView.findViewById(R.id.textViewTitle);
            viewHolder.span = (TextView)convertView.findViewById(R.id.textViewSpan);
            viewHolder.total = (TextView)convertView.findViewById(R.id.textViewTotal);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.title.setText(String.format("KW %02d", week.getWeekNumber()));
        viewHolder.total.setText(TimeFormatHelper.formatTimeOptional(week.getTotalAttendance()));
        viewHolder.span.setText(week.formatSpan());

        return convertView;
    }
}
