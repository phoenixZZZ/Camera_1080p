package com.jiuan.it.ipc.ui.animation;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.jiuan.it.ipc.R;

public class AnimationConfig {

    public static void shake(Context context,View view) {
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        view.startAnimation(shake);
    }

}
