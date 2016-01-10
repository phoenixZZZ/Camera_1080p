package com.jiuan.it.ipc.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.ui.graphics.SelectorDrawable;

public class CustomImageView extends ImageView {

    public CustomImageView(Context context) {
        super(context);
        init(context, null);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setCustomImageDrawable(Drawable drawable) {
        setImageDrawable(new SelectorDrawable(drawable));
    }

    public void setCustomImageBitmap(Bitmap bitmap) {
        setImageBitmap(bitmap);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomImageView);
        Drawable cuteSrc = typedArray.getDrawable(R.styleable.CustomImageView_custom_src);
        typedArray.recycle();
        if (cuteSrc != null) {
            setCustomImageDrawable(new SelectorDrawable(cuteSrc));
        }
    }

}
