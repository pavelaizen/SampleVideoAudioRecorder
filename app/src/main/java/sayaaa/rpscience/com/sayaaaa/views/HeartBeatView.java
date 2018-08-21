package sayaaa.rpscience.com.sayaaaa.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import sayaaa.rpscience.com.sayaaaa.R;

public class HeartBeatView extends AppCompatImageView {

    private static final String TAG = "HeartBeatView";

    public HeartBeatView(Context context) {
        super(context);
        init();
    }

    public HeartBeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeartBeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //make this not mandatory
        setLayerType(LAYER_TYPE_HARDWARE, null);
        Drawable heartDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_heart_red_24dp);
        setImageDrawable(heartDrawable);

    }

    private int getPumpAnimationSpeed(int rate){
        return (int) ((float)1000*60/rate/2);
    }

    public void pumpAnimation(int hr) {
        int animationDuration = getPumpAnimationSpeed(hr);
        if (getAnimation() == null) {
            Animation anim = new ScaleAnimation(1f, 1.3f, 1f, 1.3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(animationDuration);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            startAnimation(anim);
        }else {
            getAnimation().setDuration(animationDuration);
        }
    }





}