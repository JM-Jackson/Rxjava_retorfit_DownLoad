package download.comagic.com.rxjava_retorfit_download;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import download.comagic.com.rxjava_retorfit_download.Download.ApkReceiverListener;

/**
 * @author leiyuanxin
 * @create 2018/8/6
 * @Describe
 */
public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    private List<AppStoreBean > datas = new ArrayList<>();
    private ApkReceiverListener apkReceiverListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);

        AppStoreBean data = new AppStoreBean();
        data.name = "酷狗音乐";
        data.packageName = "com.kugou.android";
        data.apkName = "kugou.apk";
        data.introduce = "人气音乐播放器，海量曲库";
        data.version = "9.0.1";
        data.size = 0;
//        http://ucdl.25pp.com/fs08/2018/07/23/2/2_0c47d77d3c65b330e5e885f7089f85e8.apk?seq=1533538029647
        data.downloadUrl = "/fs08/2018/07/23/2/2_0c47d77d3c65b330e5e885f7089f85e8.apk?seq=1533538029647";
        data.iconUrl = "http://android-artworks.25pp.com/fs08/2018/07/25/2/110_6404427c8c430a988a448f60a00093eb_con.png";

        AppStoreBean data2 = new AppStoreBean();
        data2.name = "火山小视频";
        data2.packageName = "com.ss.android.ugc.live";
        data2.apkName = "huoshan.apk";
        data2.introduce = "每一刻都值得铭记";
        data2.version = "4.4.5";
        data2.size = 0;
//        http://ucdl.25pp.com/fs08/2018/08/03/0/110_3f9e25e808ef0543c8bb3d221acead71.apk?seq=1533538111249
        data2.downloadUrl = "/fs08/2018/08/03/0/110_3f9e25e808ef0543c8bb3d221acead71.apk?seq=1533538111249";
        data2.iconUrl = "http://android-artworks.25pp.com/fs08/2018/08/03/3/110_628c5a742e2246a641c29ba0b4f08a66_con.png";
        datas.add(data);
        datas.add(data2);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        final AppStoreAdapter adapter = new AppStoreAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setDatas(datas);

        apkReceiverListener = new ApkReceiverListener(this);
        apkReceiverListener.setApkListener(new ApkReceiverListener.ApkListener() {
            @Override
            public void installation(String packageName) {
                for (int i = 0; i < datas.size(); i++) {
                    AppStoreBean data = datas.get(i);
                    if (data.packageName.equals(packageName)){
                        adapter.notifyItemChanged(i,"installation");
                    }
                }
            }

            @Override
            public void update(String packageName) {
                for (int i = 0; i < datas.size(); i++) {
                    AppStoreBean data = datas.get(i);
                    if (data.packageName.equals(packageName)){
                        adapter.notifyItemChanged(i,"update");
                    }
                }
            }

            @Override
            public void remove(String packageName) {
                for (int i = 0; i < datas.size(); i++) {
                    AppStoreBean data = datas.get(i);
                    if (data.packageName.equals(packageName)){
                        adapter.notifyItemChanged(i,"remove");
                    }
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //去掉广播
        apkReceiverListener.unregisterReceiver();
    }
}
