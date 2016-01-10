package com.jiuan.it.ipc.common.textwatcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.jiuan.it.ipc.common.util.ZnkActivityUtil;

public class ShowClearImgTextWatcher implements TextWatcher,View.OnClickListener {

    @Override
    public void onClick(View v) {
        if (v.getId() == showView.getId()) {
            ZnkActivityUtil.clearEditText(inputEditText.getId());
            showView.setVisibility(View.GONE);
        }
    }

    private EditText inputEditText;
    private View showView;

    public ShowClearImgTextWatcher(EditText inputEditText, View tagView) {
        super();
        this.inputEditText = inputEditText;
        this.showView = tagView;
        showView.setOnClickListener(this);
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
        showView.setVisibility(View.GONE);
        if (inputEditText.getText().toString().length() > 0) {
            showView.setVisibility(View.VISIBLE);
        }
    }

}
