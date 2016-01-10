package com.jiuan.it.ipc.ui.animation;

import android.animation.ObjectAnimator;
import android.view.View;

public class SlideInUpAnimator extends BaseViewAnimator {

    private float mTargetY;

    /**
     * 要在prepare之前调用
     */
    public void setTargetY(float targetY) {
        mTargetY = targetY;
    }

    @Override
    public void prepare(View target) {
        getAnimatorAgent().playTogether(
                ObjectAnimator.ofFloat(target, "alpha", 1, 1),
                ObjectAnimator.ofFloat(target, "translationY", target.getTranslationY(), mTargetY)
        );
    }

}
