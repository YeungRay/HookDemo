package me.yl.hookdemo;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by RayYeung on 2016/12/27.
 */

public class HookUtils {

    private Context context;
    private Class<?> proxyActivity;
    //系统程序入口ActivityThread的对象
    private Object activityThreadValue;

    public HookUtils(Context context, Class<?> proxyActivity) {
        this.context = context;
        this.proxyActivity = proxyActivity;
    }

    public static final String TAG = "HookUtils";

    public void hookAms() throws Exception {
        Log.i(TAG, "start hook ams");
        Class<?> forName = Class.forName("android.app.ActivityManagerNative");
        Field defaultField = forName.getDeclaredField("gDefault");
        defaultField.setAccessible(true);
        Object defaultValue = defaultField.get(null);
        Class<?> forName1 = Class.forName("android.util.Singleton");
        Field instanceField = forName1.getDeclaredField("mInstance");
        instanceField.setAccessible(true);
        Object iActivityManagerObject = instanceField.get(defaultValue);
        Class<?> iActivityManagerIntercept = Class.forName("android.app.IActivityManager");
        InvocationHandler handler = new DynamicSubject(iActivityManagerObject);
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{iActivityManagerIntercept}, handler);
        instanceField.set(defaultValue, proxy);
    }

    private class DynamicSubject implements InvocationHandler {

        private Object target;

        public DynamicSubject(Object obj) {
            this.target = obj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.i(TAG, "method name : " + method.getName());
            if (method.getName().equals("startActivity")) {
                Log.i(TAG, " 偷天换日 " );
                Intent intent = null;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        intent = (Intent) args[i];//原意图
                        index = i;
                        break;
                    }
                }
                Intent proxyIntent = new Intent();
                ComponentName componentName = new ComponentName(context, proxyActivity);
                proxyIntent.setComponent(componentName);
                proxyIntent.putExtra("oldIntent", intent);
                args[index] = proxyIntent;
                return method.invoke(target, args);
            }
            return method.invoke(target, args);
        }
    }


    public void hookSystemHandler() throws Exception {
        Log.i(TAG, "start hook handler");
        Class<?> forName = Class.forName("android.app.ActivityThread");
        Field currentActivityThread = forName.getDeclaredField("sCurrentActivityThread");
        currentActivityThread.setAccessible(true);
        activityThreadValue = currentActivityThread.get(null);
        Field handlerField = forName.getDeclaredField("mH");
        handlerField.setAccessible(true);
        //mH变量的值
        Handler handlerObject = (Handler) handlerField.get(activityThreadValue);
        Field callbackField = Handler.class.getDeclaredField("mCallback");
        callbackField.setAccessible(true);
        callbackField.set(handlerObject, new ActivityThreadHandlerCallback());
    }


    private class ActivityThreadHandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            //public static final int LAUNCH_ACTIVITY         = 100;
            if (msg.what == 100) {
                Log.i(TAG, "message callback launch Activity");
                handleLaunchActivity(msg);
            }
            return false;
        }

        private void handleLaunchActivity(Message msg) {
            Object obj = msg.obj;//ActivityClientRecord
            try {
                Field intentField = obj.getClass().getDeclaredField("intent");
                intentField.setAccessible(true);
                Intent proxyIntent = (Intent) intentField.get(obj);
                Intent realIntent = proxyIntent.getParcelableExtra("oldIntent");
                if (realIntent != null) {
                    Log.i(TAG, "替换回来");
                    proxyIntent.setComponent(realIntent.getComponent());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void hookStartActivity() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Field currentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
        currentActivityThreadField.setAccessible(true);
        Object currentActivityThread = currentActivityThreadField.get(null);
        Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
        instrumentationField.setAccessible(true);
        Instrumentation instrumentation = (Instrumentation) instrumentationField.get(currentActivityThread);
        Instrumentation myInstrumentation = new MyInstrumentation(instrumentation);
        instrumentationField.set(currentActivityThread,myInstrumentation);
    }

}
