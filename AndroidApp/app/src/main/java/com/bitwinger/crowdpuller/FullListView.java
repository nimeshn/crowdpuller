package com.bitwinger.crowdpuller;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * A subclass of ListView that will always show the full list of elements.
 * This allows a ListView to be embedded inside a ScrollView.
 */
public class FullListView extends ListView {

    public FullListView(Context context) {
        super(context);
    }

    public FullListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public FullListView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    public void resetHeight() {
        int width = getMeasuredWidth();
        int height = 0;
        ListAdapter adapter = getAdapter();
        int count = adapter != null ? adapter.getCount() : 0;
        for (int i = 0; i < count; i++) {
            View childView = adapter.getView(i, null, this);
            childView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            height += childView.getMeasuredHeight();
            Log.i("Measures:", Integer.toString(height));
        }
        height += getDividerHeight() * (count - 1);
        Log.i("Measures total:", Integer.toString(height));
        //
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = height + (getDividerHeight() * adapter.getCount());
        setLayoutParams(params);
        requestLayout();
    }
}
