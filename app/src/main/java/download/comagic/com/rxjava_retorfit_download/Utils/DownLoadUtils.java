package download.comagic.com.rxjava_retorfit_download.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.io.File;

import download.comagic.com.rxjava_retorfit_download.Download.DowmLoadModel;
import download.comagic.com.rxjava_retorfit_download.MyApplication;

/**
 * @author leiyuanxin
 * @create 2018/8/2
 * @Describe
 */
public class DownLoadUtils {
    /**
     * 判断APK是否安装
     *
     * @param packageName
     * @return
     */
    public static boolean isAppInstalled(String packageName) {
        PackageManager pm = MyApplication.appContext.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    /**
     * 打开应用
     * @param info
     */
    public static void openApk(Context mContext, DowmLoadModel info) {
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(info.packgerName);
        mContext.startActivity(intent);
    }

    /**
     * 安装应用
     * @param info
     */
    public static void installApk(Context mContext, DowmLoadModel info) {
        File apkFile = new File(info.pathFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }


    /**
     * 获取版本APK的版本名称
     * @param packageName
     * @return
     */
    public static String apkVersionName(String packageName) {
        PackageManager pm = MyApplication.appContext.getPackageManager();
        String versionName;
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
             versionName =  packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            versionName = "";
        }

        return versionName;
    }

}
