package download.comagic.com.rxjava_retorfit_download.Utils;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {
	/** 关闭流 */
	public static boolean close(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException e) {
				LogUtil.error(e);
			}
		}
		return true;
	}
}
