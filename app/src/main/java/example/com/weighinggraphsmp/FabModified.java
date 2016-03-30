package example.com.weighinggraphsmp;


import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import com.gordonwong.materialsheetfab.AnimatedFab;

public class FabModified extends FloatingActionButton implements AnimatedFab{


    public FabModified(Context context) {
        super(context);
    }

    public FabModified(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FabModified(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void show(float translationX, float translationY) {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void show() {
        show(0, 0);
    }

    @Override
    public void hide() {
        setVisibility(View.INVISIBLE);
    }
}
