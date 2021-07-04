package com.example.messageviewapp.view;

import android.content.Context;
import android.util.Log;

public class StatusBarUtil {

    public static int getStatusBarHeight(Context context) {
        int height = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            height = context.getResources().getDimensionPixelSize(resId);
        }
        Log.d("StatusBarUtil", "StatusBarHeight = " + height);
        return height;
    }
}
