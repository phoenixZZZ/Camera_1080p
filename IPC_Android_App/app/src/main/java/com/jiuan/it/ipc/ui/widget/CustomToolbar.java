package com.jiuan.it.ipc.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.jiuan.it.ipc.R;

public class CustomToolbar extends Toolbar implements View.OnClickListener {

    public interface OnClickCustomToolbarListener {

        void onClickLeft();

        void onClickRight();
    }

    private OnClickCustomToolbarListener mOnClickCustomToolbarListener;

    private RelativeLayout mLayout;

    private TextView mTitle;

    private TextView mTextLeft;

    private TextView mTextRight;

    private CustomImageView mImageLeft;

    private CustomImageView mImageRight;

    public CustomToolbar(Context context) {
        super(context);
        init(context, null);
    }

    public CustomToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public void onClick(View v) {
        if (mOnClickCustomToolbarListener == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.custom_tv_left:
                mOnClickCustomToolbarListener.onClickLeft();
                break;
            case R.id.custom_tv_right:
                mOnClickCustomToolbarListener.onClickRight();
                break;
            case R.id.custom_iv_left:
                mOnClickCustomToolbarListener.onClickLeft();
                break;
            case R.id.custom_iv_right:
                mOnClickCustomToolbarListener.onClickRight();
                break;
        }
    }

    public void setOnClickCuteToolbarListener(OnClickCustomToolbarListener onClickCustomToolbarListener) {
        mOnClickCustomToolbarListener = onClickCustomToolbarListener;
    }

    public String getCustomTitle() {
        return mTitle.getText().toString();
    }

    public void setCustomTitle(String title) {
        mTitle.setText(title);
    }

    public void setCustomTitle(int resId) {
        mTitle.setText(resId);
    }

    public void setTextLeft(String text) {
        mTextLeft.setText(text);
    }

    public void setTextRight(String text) {
        mTextRight.setText(text);
    }

    public void setImageLeft(Drawable drawable) {
        mImageLeft.setCustomImageDrawable(drawable);
    }

    public void setImageRight(Drawable drawable) {
        mImageRight.setCustomImageDrawable(drawable);
    }

    public void setTextColorLeft(int color) {
        mTextLeft.setTextColor(color);
    }

    public void setTextColorRight(int color) {
        mTextRight.setTextColor(color);
    }

    public RelativeLayout getLayout(){
        return mLayout;
    }

    private void init(Context context, AttributeSet attrs) {
        setContentInsetsAbsolute(0, 0);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomToolbar);
        String customTitle = typedArray.getString(R.styleable.CustomToolbar_custom_title);
        String customLeftText = typedArray.getString(R.styleable.CustomToolbar_custom_left_text);
        String customRightText = typedArray.getString(R.styleable.CustomToolbar_custom_right_text);
        Drawable customLeftSrc = typedArray.getDrawable(R.styleable.CustomToolbar_custom_left_src);
        Drawable customRightSrc = typedArray.getDrawable(R.styleable.CustomToolbar_custom_right_src);
        int color = typedArray.getColor(R.styleable.CustomToolbar_custom_text_color, 0);
        int background = typedArray.getColor(R.styleable.CustomToolbar_custom_background,255);
        typedArray.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_custom_toolbar, this);
        mLayout = (RelativeLayout) findViewById(R.id.custom_layout);
        mTitle = (TextView) findViewById(R.id.custom_toolbar_title);
        mTextLeft = (TextView) findViewById(R.id.custom_tv_left);
        mTextRight = (TextView) findViewById(R.id.custom_tv_right);
        mImageLeft = (CustomImageView) findViewById(R.id.custom_iv_left);
        mImageRight = (CustomImageView) findViewById(R.id.custom_iv_right);

        setVisibility(mTitle, customTitle,color);
        setVisibility(mTextLeft, customLeftText,color);
        setVisibility(mTextRight, customRightText,color);
        setVisibility(mImageLeft, customLeftSrc);
        setVisibility(mImageRight, customRightSrc);

        setBackgroundColor(background);
        mTextLeft.setOnClickListener(this);
        mTextRight.setOnClickListener(this);
        mImageLeft.setOnClickListener(this);
        mImageRight.setOnClickListener(this);
    }

    private void setVisibility(TextView view, String text,int color) {
        if (!TextUtils.isEmpty(text)) {
            view.setText(text);
            view.setTextColor(color);
            view.setVisibility(View.VISIBLE);
        }
    }

    private void setVisibility(CustomImageView view, Drawable drawable) {
        if (drawable != null) {
            view.setCustomImageDrawable(drawable);
            view.setVisibility(View.VISIBLE);
        }
    }

}
