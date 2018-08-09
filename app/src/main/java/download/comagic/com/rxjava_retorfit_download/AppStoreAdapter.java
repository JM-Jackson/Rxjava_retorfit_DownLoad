package download.comagic.com.rxjava_retorfit_download;

import android.content.Context;
import android.net.Uri;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import download.comagic.com.rxjava_retorfit_download.Download.DowmLoadModel;
import download.comagic.com.rxjava_retorfit_download.Download.DownLoadManager;
import download.comagic.com.rxjava_retorfit_download.Download.DownLoadObserver;
import download.comagic.com.rxjava_retorfit_download.Utils.DownLoadUtils;
import retrofit2.http.Url;

/**
 * @author leiyuanxin
 * @create 2018/7/31
 * @Describe
 */
public class AppStoreAdapter extends RecyclerView.Adapter {


    List<AppStoreBean> datas = new ArrayList<>();
    Context mContext;


    public AppStoreAdapter(Context context) {
        mContext = context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_app_store, null));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AppStoreBean data = datas.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
//        viewHolder.imgIcon.setImageURI(Uri.parse(data.iconUrl));
        Glide.with(mContext).load(data.iconUrl).into(viewHolder.imgIcon);

        viewHolder.tvName.setText(data.name);
        viewHolder.introduce.setText(data.introduce);
        final DowmLoadModel model = DownLoadManager.getInstance().getDowmLoadModel(data);

        viewHolder.model = model;
        DownLoadManager.getInstance().addObserver(viewHolder);
        viewHolder.refreshCircleProgressViewUI(model);

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public List<AppStoreBean> getDatas() {
        return datas;
    }

    public void setDatas(List<AppStoreBean> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements DownLoadObserver {
        @BindView(R.id.imgIcon)
        ImageView imgIcon;
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.introduce)
        TextView introduce;
        @BindView(R.id.tvStatus)
        Button tvStatus;
        @BindView(R.id.progressBar)
        ProgressBar progressBar;
        View view;

        DowmLoadModel model;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            view = itemView;
            tvStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Click(model);
                }
            });
        }

        //状态改变监听
        @Override
        public void onDownLoadInfoChange(final DowmLoadModel info) {
            // 过滤DownLoadInfo
            if (!info.packgerName.equals(model.packgerName)) {
                return;
            }
            postTaskSafely(new Runnable() {
                @Override
                public void run() {
                    //重置整个按钮
                    model = info;
                    refreshCircleProgressViewUI(info);
                }
            });
        }

        /**
         * 安全的执行一个任务
         */
        public static void postTaskSafely(Runnable task) {
            int curThreadId = Process.myTid();
            // 如果当前线程是主线程
            if (curThreadId == MyApplication.mMainTreadId) {
                task.run();
            } else {// 如果当前线程不是主线程
                MyApplication.mHandler.post(task);
            }

        }

        public void refreshCircleProgressViewUI(DowmLoadModel info) {
            switch (info.status) {
                /**
                 状态(编程记录)  	|  给用户的提示(ui展现)
                 ----------------|----------------------
                 未下载			|下载
                 下载中			|显示进度条
                 暂停下载			|继续下载
                 等待下载			|等待中...
                 下载失败 			|重试
                 下载完成 			|安装
                 已安装 			|打开
                 */
                // 未下载
                case DownLoadManager.STATUS_NOLOAD:
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("下载");
                    break;
                // 下载中
                case DownLoadManager.STATUS_DOWNLOADING:
                    tvStatus.setText("下载中...");
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setMax(100);
                    int progress = (int) (info.progress * 100.f / info.maxSize + .5f);
                    progressBar.setProgress(progress);
                    break;
                // 暂停下载
                case DownLoadManager.STATUS_STOP:
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setMax(100);
                    int pro = (int) (info.progress * 100.f / info.maxSize + .5f);
                    progressBar.setProgress(pro);
                    tvStatus.setText("继续下载");
                    break;
                // 等待下载
                case DownLoadManager.STATUS_WAIT:
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("等待中...");
                    break;
                // 下载失败
                case DownLoadManager.STATUS_FAILURE:
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("重试");
                    break;
                // 下载完成
                case DownLoadManager.STATUS_COMPLETED:
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("安装");
                    break;
                // 已安装
                case DownLoadManager.STATUS_INSTALLED:
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("打开");
                    break;
                // 发现新版本需要更新
                case DownLoadManager.STATUS_UPDATE:
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("更新");
                    break;

                default:
                    break;
            }
        }


        private void Click(DowmLoadModel info) {
            switch (info.status) {
                /**
                 状态(编程记录)     | 用户行为(触发操作)
                 ----------------| -----------------
                 未下载			| 去下载
                 下载中			| 暂停下载
                 暂停下载			| 断点继续下载
                 等待下载			| 取消下载
                 下载失败 			| 重试下载
                 下载完成 			| 安装应用
                 已安装 			| 打开应用
                 */
                // 未下载
                case DownLoadManager.STATUS_NOLOAD:
                    DownLoadManager.getInstance().downLoad(info);
                    break;
                // 下载中
                case DownLoadManager.STATUS_DOWNLOADING:
                    DownLoadManager.getInstance().pause(info);

                    break;
                // 暂停下载  则重新继续下载
                case DownLoadManager.STATUS_STOP:
                    DownLoadManager.getInstance().downLoad(info);

                    break;
                // 等待下载
                case DownLoadManager.STATUS_WAIT:
                    DownLoadManager.getInstance().cancel(info);

                    break;
                // 下载失败
                case DownLoadManager.STATUS_FAILURE:
                    DownLoadManager.getInstance().downLoad(info);

                    break;
                // 下载完成
                case DownLoadManager.STATUS_COMPLETED:
                    DownLoadUtils.installApk(MyApplication.appContext, info);

                    break;
                // 已安装
                case DownLoadManager.STATUS_INSTALLED:
                    DownLoadUtils.openApk(MyApplication.appContext, info);

                    break;
                //发现新版本进行下载更新
                case DownLoadManager.STATUS_UPDATE:
                    DownLoadManager.getInstance().downLoad(info);
                    break;


                default:
                    break;
            }
        }

    }


}
