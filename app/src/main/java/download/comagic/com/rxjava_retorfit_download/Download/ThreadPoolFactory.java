package download.comagic.com.rxjava_retorfit_download.Download;


/**
 *   线程池工厂
 */
public class ThreadPoolFactory {
	static ThreadPoolProxy	mDownLoadPool;

	/**得到一个下载的线程池*/
	public static ThreadPoolProxy getDownLoadPool() {
		if (mDownLoadPool == null) {
			synchronized (ThreadPoolProxy.class) {
				if (mDownLoadPool == null) {
					mDownLoadPool = new ThreadPoolProxy(3, 3, 3000);
				}
			}
		}
		return mDownLoadPool;
	}
}
