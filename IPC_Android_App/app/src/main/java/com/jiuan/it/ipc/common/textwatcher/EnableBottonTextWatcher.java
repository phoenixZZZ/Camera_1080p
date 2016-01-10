package com.jiuan.it.ipc.common.textwatcher;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class EnableBottonTextWatcher implements TextWatcher {

    private EditText[] inputEditText;
    private View[] views;

    public EnableBottonTextWatcher(View[] views, EditText[] inputEditText) {
        super();
        this.inputEditText = inputEditText;
        this.views = views;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {

        if (views == null) {
            return;
        }

        boolean isEmpty = false;

        for (EditText editText : inputEditText) {
            if (TextUtils.isEmpty(editText.getText())) {
                isEmpty = true;
                break;
            }
        }

        for (View tagView : views) {

            if (isEmpty) {
                tagView.setEnabled(false);
            } else {
                tagView.setEnabled(true);
            }

        }

    }

}
