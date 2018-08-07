package download.comagic.com.rxjava_retorfit_download.api;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * 创建时间： 2017/9/8.
 * 创建人： leiyuanxin
 * 描述：请求－接口
 */
public interface ServerAPI {

    @Streaming
    @GET
    Observable<ResponseBody> download(@Header("Range") String range, @Url() String url);
}
