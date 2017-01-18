package me.yl.hookdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

/**
 * Created by RayYeung on 2016/12/27.
 */

public class OtherActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("I  am   other  Activity");
        setContentView(tv);
    }
}
