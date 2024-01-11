package com.student.sleepcycle;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageButton;

public class RecordButton extends AppCompatImageButton {

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.shape_oval_button);
    }

    public void updateIconWithState(com.student.sleepcycle.sleeping.State state) {
        switch (state) {
            case BEFORE_RECORDING:
                setImageResource(R.drawable.ic_recorde);
                break;
            case ON_RECORDING:
                setImageResource(R.drawable.ic_stop);
                break;
            case AFTER_RECORDING:
                setImageResource(R.drawable.ic_play);
                break;

        }
    }
}