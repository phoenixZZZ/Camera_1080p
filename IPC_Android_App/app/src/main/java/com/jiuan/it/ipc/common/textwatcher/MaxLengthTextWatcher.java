package com.jiuan.it.ipc.common.textwatcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MaxLengthTextWatcher implements TextWatcher {

    private EditText inputEditText;
    private int byteLength;
    private String beforeString;

    public MaxLengthTextWatcher( EditText inputEditText,int byteLength) {
        super();
        this.inputEditText = inputEditText;
        this.byteLength = byteLength;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        beforeString = s.toString();
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (inputEditText.getText().toString().getBytes().length > byteLength) {
            inputEditText.setText(beforeString);
        }
    }

}
