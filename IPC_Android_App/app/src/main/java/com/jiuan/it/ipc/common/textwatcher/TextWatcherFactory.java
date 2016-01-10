package com.jiuan.it.ipc.common.textwatcher;

import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class TextWatcherFactory {


    public enum TextWatcherTyp{
        ShowClearImgTextWatcher,
        EnableBottonTextWatcher,
        MaxLengthTextWatcher,
        HideViewTextWatcher
    }

    private TextWatcherFactory() {}

    // EnableBottonTextWatcher
    public static TextWatcher getTextWatcher(EditText inputEditText[], View tagView, TextWatcherTyp type) {

        return getTextWatcher(inputEditText, 0, new View[] {tagView }, type);
    }

    // ShowClearImgTextWatcher
    public static TextWatcher getTextWatcher(EditText inputEditText, View view, TextWatcherTyp type) {

        return getTextWatcher(new EditText[] { inputEditText }, 0, new View[] { view }, type);
    }

    // MaxLengthTextWatcher
    public static TextWatcher getTextWatcher(EditText inputEditText, int length, TextWatcherTyp type) {

        return getTextWatcher(new EditText[] { inputEditText }, length, null, type);

    }

    // EnableViewTextWatcher
    public static TextWatcher getTextWatcher(EditText inputEditText, View[] tagView, TextWatcherTyp type) {

        return getTextWatcher(new EditText[] { inputEditText }, 0, tagView, type);

    }

    public static TextWatcher getTextWatcher(EditText[] inputEditText, int length, View[] tagView, TextWatcherTyp textWatcherTyp) {
        TextWatcher textWatcher = null;
        switch (textWatcherTyp){

            case ShowClearImgTextWatcher:
                textWatcher = new ShowClearImgTextWatcher(inputEditText[0], tagView[0]);
                break;
            case EnableBottonTextWatcher:
                textWatcher = new EnableBottonTextWatcher(tagView, inputEditText);
                break;
            case MaxLengthTextWatcher:
                textWatcher = new MaxLengthTextWatcher(inputEditText[0], length);
                break;
            case HideViewTextWatcher:
                textWatcher = new EnableBottonTextWatcher(tagView, inputEditText);
                break;
        }

        return textWatcher;
    }

}
