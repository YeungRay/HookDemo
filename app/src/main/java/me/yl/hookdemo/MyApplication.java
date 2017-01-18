package me.yl.hookdemo;

import android.app.Application;

/**
 * Created by RayYeung on 2016/12/27.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HookUtils utils = new HookUtils(this,ProxyActivity.class);
        try {
            utils.hookStartActivity();
            utils.hookAms();
            utils.hookSystemHandler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


