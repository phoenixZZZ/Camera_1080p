package com.jiuan.it.ipc.ui.graphics;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

public class SelectorDrawable extends StateListDrawable {

    public SelectorDrawable(Drawable drawable) {
        super();
        addState(new int[]{}, drawable);
    }

    @Override
    protected boolean onStateChange(int[] states) {
        boolean isClicked = false;
        for (int state : states) {
            if (state == android.R.attr.state_pressed) {
                isClicked = true;
            }
        }
        if (isClicked)
            setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        else
            clearColorFilter();

        return super.onStateChange(states);
    }
}
