package me.yl.hookdemo;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

import static me.yl.hookdemo.HookUtils.TAG;

/**
 * 静态代理类
 * Created by RayYeung on 2017/1/18.
 */

public class MyInstrumentation extends Instrumentation {

    private Instrumentation mBase;

    public MyInstrumentation(Instrumentation mBase) {
        this.mBase = mBase;
    }


    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
                Log.d(TAG, "\n执行了startActivity, 参数如下: \n" + "who = [" + who + "], " +
                        "\ncontextThread = [" + contextThread + "], \ntoken = [" + token + "], " +
                        "\ntarget = [" + target + "], \nintent = [" + intent +
                        "], \nrequestCode = [" + requestCode + "], \noptions = [" + options + "]");
                try {
                    Method execStartActivityMethod = Instrumentation.class.getDeclaredMethod(
                            "execStartActivity",
                            Context.class, IBinder.class, IBinder.class, Activity.class,
                            Intent.class, int.class, Bundle.class);
                    execStartActivityMethod.setAccessible(true);
                    return (ActivityResult) execStartActivityMethod.invoke(mBase,who,contextThread,token,target,intent,requestCode,options);
                } catch (Exception e) {
                    e.printStackTrace();
        }

        return  null;
    }

}
