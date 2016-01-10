package com.jiuan.it.ipc.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.tools.DensityUtils;

import java.lang.reflect.Field;

public class ShowDialogFragment extends DialogFragment  {

    private TextView textView;

    private String message;

    private int visibility;

    private String positive;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);//设置样式
        message =  getArguments().getString("message");
        visibility =  getArguments().getInt("visibility", View.VISIBLE);
        positive =  getArguments().getString("positive", getActivity().getResources().getString(R.string.dialog_fragment_share));
    }
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null)
            return;
        getDialog().getWindow().setLayout(DensityUtils.dp2px(getActivity(), 300),
                DensityUtils.dp2px(getActivity(),200));
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return true;
            }
        });
        AlertDialog dialog = (AlertDialog) getDialog();
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object alertController = mAlert.get(dialog);

            Field mButtonPositive = alertController.getClass().getDeclaredField("mButtonPositive");
            mButtonPositive.setAccessible(true);
            Button positive = (Button) mButtonPositive.get(alertController);
            positive.setTextColor(0xff61a4ea);

            Field mButtonNegative = alertController.getClass().getDeclaredField("mButtonNegative");
            mButtonNegative.setAccessible(true);
            Button negative = (Button) mButtonNegative.get(alertController);
            negative.setTextColor(0xff61a4ea);
            negative.setVisibility(visibility);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog_show, null);
        textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(message);
        builder.setView(view)
                .setPositiveButton(positive,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                mShowFragmentOnListener.onConfirmClickListener();
                            }
                        })
                .setNegativeButton(R.string.dialog_fragment_cancel, null);
        return builder.create();
    }


    public interface ShowFragmentOnListener {
        void onConfirmClickListener();

    }
    protected ShowFragmentOnListener mShowFragmentOnListener;

    public void setShowFragmentOnListener(ShowFragmentOnListener showFragmentOnListener) {
        mShowFragmentOnListener = showFragmentOnListener;
    }



}
