package download.comagic.com.rxjava_retorfit_download.Download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.widget.Toast;

/**
 * @author leiyuanxin
 * @create 2018/8/7
 * @Describe  apk安装卸载更新监听
 */
public class ApkReceiverListener {

    private  BroadcastReceiver apkInstallListener;
    private  Context mContext;

    public ApkReceiverListener (Context context) {
        mContext = context;
        initReceiver();
        registerSDCardListener();
    }

    private  void initReceiver() {
        apkInstallListener = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                PackageManager manager = context.getPackageManager();
                if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                    String packageName = intent.getData().getSchemeSpecificPart();
//                    Toast.makeText(context, "安装成功"+packageName, Toast.LENGTH_LONG).show();
                    if (null!=apkListener){
                        apkListener.installation(packageName);
                    }
                }
                if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                    String packageName = intent.getData().getSchemeSpecificPart();
//                    Toast.makeText(context, "卸载成功"+packageName, Toast.LENGTH_LONG).show();

                    if (null!=apkListener){
                        apkListener.remove(packageName);
                    }
                }
                if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                    String packageName = intent.getData().getSchemeSpecificPart();
//                    Toast.makeText(context, "替换成功"+packageName, Toast.LENGTH_LONG).show();

                    if (null!=apkListener){
                        apkListener.update(packageName);
                    }
                }
            }
        };
    }
    //接口回调
    public interface  ApkListener{
        /**
         * 安装了APK
         * @param packageName
         */
        public void installation(String packageName);

        /**
         * 更新了APK
         * @param packageName
         */
        public void update(String packageName);

        /**
         * 移除了APK
         * @param packageName
         */
        public void remove(String packageName);

    }
    private ApkListener apkListener;

    public void setApkListener(ApkListener apkListener) {
        this.apkListener = apkListener;
    }

    // 注册监听
    private void registerSDCardListener(){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        mContext.registerReceiver(apkInstallListener, intentFilter);
    }
    //取消监听
    public void unregisterReceiver(){
        mContext.unregisterReceiver(apkInstallListener);
    }

}
