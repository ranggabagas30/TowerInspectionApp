package com.sap.inspection.view.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ImageViewForList extends ImageView {
    public ImageViewForList(Context context) {
        super(context);
    }

    public ImageViewForList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewForList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setPressed(boolean pressed) {
        // Make sure the parent is a View prior casting it to View
        if (pressed && getParent() instanceof View && ((View) getParent()).isPressed()) {
            return;
        }
        super.setPressed(pressed);
    }
    
}
