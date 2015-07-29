package de.nnscr.attendance.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.nnscr.attendance.R;

/**
 * Created by philipp on 28.07.15.
 */
public class SimpleCard extends CardView {
    TextView captionView;
    TextView valueView;

    public SimpleCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SimpleCard(Context context) {
        super(context);
        initView();
    }

    public SimpleCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setUseCompatPadding(true);
        setRadius(getResources().getDimension(R.dimen.card_radius));
        setElevation(getResources().getDimension(R.dimen.card_elevation));

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.simple_card, this);

        captionView = (TextView)findViewById(R.id.caption);
        valueView = (TextView)findViewById(R.id.value);
    }

    public void setCaption(String caption) {
        captionView.setText(caption);
        invalidate();
        requestLayout();
    }

    public void setValue(String value) {
        valueView.setText(value);
        invalidate();
        requestLayout();
    }

    public void setValueColor(int color) {
        valueView.setTextColor(color);
    }

    public void makeClickable(OnClickListener listener) {
        setClickable(true);
        setEnabled(true);
        setHapticFeedbackEnabled(true);
        setOnClickListener(listener);
        setRipple(true);
    }

    private void setRipple(boolean ripple) {
        Drawable drawable;
        if (ripple) {
            int[] attrs = new int[]{android.R.attr.selectableItemBackground};
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs);
            drawable = typedArray.getDrawable(0);
        } else {
            drawable = null;
        }

        setForeground(drawable);
    }
}
