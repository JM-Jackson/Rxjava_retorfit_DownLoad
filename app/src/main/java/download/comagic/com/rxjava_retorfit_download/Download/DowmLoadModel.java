package download.comagic.com.rxjava_retorfit_download.Download;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

/**
 * @author leiyuanxin
 * @create 2018/7/31
 * @Describe
 */
@Entity
public class DowmLoadModel {
    @Id
    public long id;

    public long apkid;
    //下载链接
    public String url;
    //存储路径
    public String pathFile;
    //下载状态
    public int status;
    //包名
    public String packgerName;
    //版本信息
    public String version;
    //进度字节
    public long progress;
    //整体最大字节
    public long maxSize;
    @Transient
    public Runnable task;
}
