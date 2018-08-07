package download.comagic.com.rxjava_retorfit_download;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import download.comagic.com.rxjava_retorfit_download.Download.MyObjectBox;
import io.objectbox.BoxStore;

/**
 * @author leiyuanxin
 * @create 2018/8/6
 * @Describe
 */
public class MyApplication extends Application{


    public static Context appContext;
    public static Thread	mMainThread;
    public static long	mMainTreadId;

    public static Handler mHandler;

    private static BoxStore boxStore;
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;

        // 主线程
        mMainThread = Thread.currentThread();

        // 主线程Id
        mMainTreadId = android.os.Process.myTid();

        // 定义一个handler

        mHandler = new Handler();

        boxStore = MyObjectBox.builder().androidContext(this).build();

    }

    public static BoxStore getBoxStore() {
        return boxStore;
    }
}
