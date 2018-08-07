package download.comagic.com.rxjava_retorfit_download.api;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author leiyuanxin
 * @create 2018/8/7
 * @Describe
 */
public class RetrofitHttp {

    private static Retrofit mRequestClient;

    private static ServerAPI apiService;

    //创建新的retorfit
    public static void init() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(new CommonInterceptor())
                .addNetworkInterceptor(new LoggingInterceptor())
                .build();
        mRequestClient = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl("http://ucdl.25pp.com")
                .build();
        apiService = mRequestClient.create(ServerAPI.class);
    }

    public static ServerAPI getApiService() {
        return apiService;
    }
}
