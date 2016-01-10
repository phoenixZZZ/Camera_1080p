package com.jiuan.it.ipc.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.jiuan.it.ipc.R;

public class CustomEditGroup extends RelativeLayout implements TextWatcher, View.OnClickListener {

    private CustomImageView mCivCancel;

    private CustomEditText mCet;

    public CustomEditGroup(Context context) {
        super(context);
        init(context, null);
    }

    public CustomEditGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomEditGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomEditGroup);
        Drawable src = typedArray.getDrawable(R.styleable.CustomEditGroup_android_src);
        int inputType = typedArray.getInt(R.styleable.CustomEditGroup_android_inputType, EditorInfo.TYPE_NULL);
        String hint = typedArray.getString(R.styleable.CustomEditGroup_android_hint);
        int maxLength = typedArray.getInt(R.styleable.CustomEditGroup_android_maxLength, 20);
        typedArray.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_custom_edit_group, this);
        ImageView image = (ImageView) findViewById(R.id.icon);
        mCivCancel = (CustomImageView) findViewById(R.id.cancel);
        mCet = (CustomEditText) findViewById(R.id.edit);
        if (isPasswordInputType(inputType)) {
            mCet.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            mCet.setInputType(inputType);
        }
        mCet.setHint(hint);
        mCet.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});//限制个数
        if (src != null) {
            image.setImageDrawable(src);
        }
        mCivCancel.setOnClickListener(this);
        mCet.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                mCet.setText(null);
                mCivCancel.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            mCivCancel.setVisibility(View.VISIBLE);
        } else {
            mCivCancel.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public String getContent() {
        return mCet.getText().toString();
    }

    private boolean isPasswordInputType(int inputType) {
        final int variation =
                inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
        return variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
                || variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
                || variation
                == (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD);
    }
}
