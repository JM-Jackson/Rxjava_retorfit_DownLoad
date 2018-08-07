package download.comagic.com.rxjava_retorfit_download.Download;

import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import download.comagic.com.rxjava_retorfit_download.AppStoreBean;
import download.comagic.com.rxjava_retorfit_download.MyApplication;
import download.comagic.com.rxjava_retorfit_download.Utils.DownLoadUtils;
import download.comagic.com.rxjava_retorfit_download.Utils.FileUtils;
import download.comagic.com.rxjava_retorfit_download.Utils.IOUtils;
import download.comagic.com.rxjava_retorfit_download.Utils.LogUtil;
import download.comagic.com.rxjava_retorfit_download.api.RetrofitHttp;
import io.objectbox.Box;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;

/**
 * @author leiyuanxin
 * @create 2018/7/31
 * @Describe
 */
public class DownLoadManager {

    private static DownLoadManager downLoadManager;

    /**
     * 任务的进行状态
     */
    //已经安装
    public static final int STATUS_INSTALLED = 0;
    //下载完成
    public static final int STATUS_COMPLETED = 1;
    //下载中
    public static final int STATUS_DOWNLOADING = 2;
    //下载失败
    public static final int STATUS_FAILURE = 3;
    //等待中
    public static final int STATUS_WAIT = 4;
    //未下载
    public static final int STATUS_NOLOAD = 5;
    //暂停下载
    public static final int STATUS_STOP = 6;

    /**
     * 用于存储所以的下载列队
     */
    Map<String, DowmLoadModel> dowmLoadModelMap = new HashMap<>();




    public static DownLoadManager getInstance() {

        if (downLoadManager == null) {
            synchronized (DownLoadManager.class) {
                if (downLoadManager == null) {
                    downLoadManager = new DownLoadManager();
                    //获取请求的实例
                    RetrofitHttp.init();
                }
            }
        }
        return downLoadManager;
    }




    /**
     * 创建下载的新线程
     */
    private class DownLoadTask implements Runnable {
        DowmLoadModel model;
        Subscription observable;
        long finalInitRange;
        Box<DowmLoadModel> notesBox;
        List<DowmLoadModel> lists;

        public DownLoadTask(DowmLoadModel model, long initRange) {
            super();
            this.model = model;
            finalInitRange = initRange;
            //获取到整个数据库
            notesBox =  MyApplication.getBoxStore().boxFor(DowmLoadModel.class);
            //根据包名查询是否有数据
            lists  = notesBox.query().equal(DowmLoadModel_.packgerName,model.packgerName).build().find();
        }

        public void unsubscribe() {
            observable.unsubscribe();
        }

        @Override
        public void run() {
            LogUtil.error("下载Url", model.url);
            LogUtil.error("下载finalInitRange", "" + finalInitRange);
//            "/qqmi/aphone_p2p/TencentVideo_V6.0.0.14297_848.apk"
            observable = new Subscriber<ResponseBody>() {
                InputStream input;
                RandomAccessFile outputStream;


                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    //下载失败
                    Toast.makeText(MyApplication.appContext,"网络中断，请检查您的网络状态",Toast.LENGTH_SHORT).show();
                    model.status = STATUS_FAILURE;
                    notesBox.put(model);
                    notifyObservers(model);
                }

                @Override
                public void onNext(ResponseBody responseBody) {
                    try {
                        //下载中
                        model.status = STATUS_DOWNLOADING;
                        notifyObservers(model);
                        //获取到流
                        input = responseBody.byteStream();
                        File file = new File(model.pathFile);

                        boolean isPause = false;
                        //
                        if (lists.size()>0){
                            DowmLoadModel sqlModel = lists.get(0);
                            //如果长度为0，则长度没有赋值 进行总长度赋值  并更新数据库
                            if (sqlModel.maxSize ==0){
                                model.maxSize =responseBody.contentLength();
                                sqlModel = model;
                                notesBox.put(sqlModel);
                            }
                        }
                        LogUtil.error("剩余下载长度", "Range"+responseBody.contentLength()+"");
                        LogUtil.error("已下载长度", "size"+file.length()+"");
                        LogUtil.error("下载路径", model.pathFile);
                        // 创建一个文件流
//                        model.progress = file.length();
                        outputStream = new RandomAccessFile(file, "rwd");
                        //在文件原本长度的基础上进行再次传输
                        outputStream.seek(file.length());
                        byte[] bytes = new byte[1024];
                        int len = -1;
                        while ((len = input.read(bytes)) != -1) {
                            //状态改变是暂停则停止
                            if (model.status == STATUS_STOP) {
                                isPause = true;
                                break;
                            }

                            outputStream.write(bytes, 0, len);
                            model.progress += len;
                            model.status = STATUS_DOWNLOADING;

                            notifyObservers(model);
                        }
                        // 用户暂停了下载走到这里来了
                        if (isPause) {
                            model.status = STATUS_STOP;
                            notesBox.put(model);
                            notifyObservers(model);
                        } else {// 下载完成走到这里来

                            model.status = STATUS_COMPLETED;
                            notesBox.put(model);
                            notifyObservers(model);
                        }


                    } catch (Exception e) {
                        //下载失败
//                        ToastUtils.showShort("下载失败，请稍后再试");
//                        LogUtil.e("下载日志", e.getMessage());
                        //如果是暂停导致 则不切换状态
                        if (e instanceof SocketException && "Socket closed".equals(e.getMessage())) {
                            model.status = STATUS_STOP;
                        } else {
                            model.status = STATUS_FAILURE;

                        }
                        notesBox.put(model);
                        notifyObservers(model);
                        e.printStackTrace();
                    } finally {
                        IOUtils.close(input);
                        IOUtils.close(outputStream);
                    }

                }
            };
            String max = "-";
            if (model.maxSize!=0){
                max +=  model.maxSize;
            }
            LogUtil.error("Rang 范围 ","bytes=" +  Long.toString(finalInitRange) + max);
            observable = RetrofitHttp.getApiService().download( "bytes=" +  Long.toString(finalInitRange) + max, model.url).subscribe((Observer<ResponseBody>) observable);

        }
    }

    /**
     * 生成模型
     *
     * @return
     */
    public DowmLoadModel getDowmLoadModel(AppStoreBean data) {
        //获取到整个数据库
        Box<DowmLoadModel> notesBox =  MyApplication.getBoxStore().boxFor(DowmLoadModel.class);
        //根据包名查询是否有数据
        List<DowmLoadModel> lists  = notesBox.query().equal(DowmLoadModel_.packgerName,data.packageName).build().find();
        DowmLoadModel model = new DowmLoadModel();
        // sdcard/android/data/包名/download
        String dir = FileUtils.getDir("download");

        File file = new File(dir+"/"+data.apkName);

        String savePath = file.getAbsolutePath();
        //查询到数据
        if (null!=lists && lists.size()>0){
            //返回数据库第一条数据
            model = lists.get(0);
        }else {
            model.packgerName = data.packageName;
            model.url = data.downloadUrl;
            model.version = data.version;
            model.maxSize = data.size;
            model.apkid = data.id;
        }

        model.pathFile = savePath;
        model.progress = file.length();

        //已经安装
        if (DownLoadUtils.isAppInstalled(data.packageName)) {
            model.status = STATUS_INSTALLED;
            notesBox.put(model);
            return model;
        }

        File saveApk = new File(model.pathFile);
        //文件已经存在了
        if (saveApk.exists()) {
            //文件大小于后台返回一致 已经下载完成
            LogUtil.error(saveApk.length()+"");
            if (saveApk.length() == model.maxSize && saveApk.length()!=0) {
                model.status = STATUS_COMPLETED;
                notesBox.put(model);
                return model;
            } else {
                //文件大小于后台返回不一致 下载暂停中
                model.status = STATUS_STOP;
                model.progress = saveApk.length();
                notesBox.put(model);
                return model;

            }
        }

        /**
         下载中
         暂停下载
         等待下载
         下载失败
         */
        DowmLoadModel downLoadInfo = dowmLoadModelMap.get(data.packageName);
        if (downLoadInfo != null) {
            notesBox.put(model);
            return downLoadInfo;
        }

        // 未下载
        model.status = STATUS_NOLOAD;
        notesBox.put(model);
        return model;
    }

    /**
     * 开始下载
     *
     * @param model
     */
    public void downLoad(final DowmLoadModel model) {
        //状态默认进来是等待中
        model.status = STATUS_WAIT;
        notifyObservers(model);
        dowmLoadModelMap.put(model.packgerName, model);
        //判断文件夹是否存在
        long initRange = 0;
        File saveApk = new File(model.pathFile);
        if (saveApk.exists()) {
            // 未下载完成的apk已有的长度
            initRange = saveApk.length();
        }
        model.progress = initRange;

        LogUtil.error("已经下载的数据：",initRange+"字节");
        // 得到线程池,执行任务
        DownLoadTask task = new DownLoadTask(model, initRange);
        // downInfo身上的task赋值
        model.task = task;
        ThreadPoolFactory.getDownLoadPool().execute(task);
    }


    /**
     * 暂停下载
     */
    public void pause(DowmLoadModel info) {
        // 找到线程池,移除任务
        DownLoadTask task = (DownLoadTask) info.task;
        //接触绑定
        task.unsubscribe();
        // 找到线程池,移除任务
        ThreadPoolFactory.getDownLoadPool().removeTask(task);

        info.status = STATUS_STOP;
        notifyObservers(info);
    }

    /**
     * 取消下载
     */
    public void cancel(DowmLoadModel info) {
        DownLoadTask task = (DownLoadTask) info.task;
        //接触绑定
        task.unsubscribe();
        // 找到线程池,移除任务
        ThreadPoolFactory.getDownLoadPool().removeTask(task);
        /*#当前状态: 未下载 #*/
        info.status = STATUS_NOLOAD;
        notifyObservers(info);
    }


    List<DownLoadObserver> downLoadObservers = new LinkedList<DownLoadObserver>();

    /**
     * 添加观察者
     */
    public void addObserver(DownLoadObserver observer) {
        if (observer == null) {
            throw new NullPointerException("observer == null");
        }
        synchronized (this) {
            if (!downLoadObservers.contains(observer)) {
                downLoadObservers.add(observer);
            }
        }
    }

    /**
     * 删除观察者
     */
    public synchronized void deleteObserver(DownLoadObserver observer) {
        downLoadObservers.remove(observer);
    }

    /**
     * 通知观察者数据改变
     */
    public void notifyObservers(DowmLoadModel info) {
        for (DownLoadObserver observer : downLoadObservers) {
            observer.onDownLoadInfoChange(info);
        }
    }

}
