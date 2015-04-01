package de.nnscr.attendance.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import de.nnscr.attendance.R;
import de.nnscr.attendance.helper.TimeFormatHelper;
import de.nnscr.attendance.model.SummaryBlock;
import de.nnscr.attendance.model.SummaryBlockHeader;
import de.nnscr.attendance.model.SummaryDay;

/**
 * Created by philipp on 31.03.15.
 */
public class SummaryBlockAdapter extends ArrayAdapter<SummaryBlock> {
    public SummaryBlockAdapter(Context context, ArrayList<SummaryBlock> model) {
        super(context, 0, model);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");
        SummaryBlock block = getItem(position);

        if (block instanceof SummaryBlockHeader) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.summary_item_detail_header, parent, false);

            TextView dateView = (TextView)convertView.findViewById(R.id.textViewDate);
            TextView totalView = (TextView)convertView.findViewById(R.id.textViewTotal);

            if (((SummaryBlockHeader)block).day != null) {
                dateView.setText(((SummaryBlockHeader) block).day.getDay().toString("dd.MM.yyyy"));
                totalView.setText(TimeFormatHelper.formatTime(((SummaryBlockHeader) block).day.getTotalAttendance()));
            }

            return convertView;
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.summary_item_detail, parent, false);

            TextView startView = (TextView) convertView.findViewById(R.id.textViewStartTime);
            TextView endView = (TextView) convertView.findViewById(R.id.textViewEndTime);
            TextView totalView = (TextView) convertView.findViewById(R.id.textViewTotal);

            long duration = (block.end.getMillis() - block.start.getMillis()) / 1000;

            startView.setText(block.start.toString(timeFormatter));
            endView.setText(block.end.toString(timeFormatter));
            totalView.setText(TimeFormatHelper.formatTime(duration));

            return convertView;
        }
    }
}
