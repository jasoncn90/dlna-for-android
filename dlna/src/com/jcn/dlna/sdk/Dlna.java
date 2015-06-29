package com.jcn.dlna.sdk;

import android.content.Context;

import com.jcn.dlna.sdk.ServiceManager.ServiceState;
import com.jcn.dlna.sdk.dmc.DeviceManager;
import com.jcn.dlna.sdk.dms.content.MediaStoreContent;

/**
 * dlna的总控制器
 */
/**
 * @author daoben
 * 
 */
public class Dlna {

	public static final int TYPE_DMC = 1;
	public static final int TYPE_DMS = 2;
	public static final int TYPE_DMC_DMS = 3;

	/**
	 * dlna操作返回结果
	 * <p>
	 * SUCCESS 操作成功<br>
	 * TOO_FREQUENTLY 操作太频繁，取消当前操作<br>
	 * FAIL 操作失败
	 */
	public static enum ActionResult {
		SUCCESS, TOO_FREQUENTLY, FAIL
	}

	private static Context context;
	private static OnDlnaStateChangeListener listener;

	public static Context getContext() {
		return Dlna.context;
	}

	public static Context getApplicationContext() {
		return context == null ? null : context.getApplicationContext();
	}

	public static OnDlnaStateChangeListener getListener() {
		return listener;
	}

	public static void init(Context context, OnDlnaStateChangeListener listener) {
		Dlna.context = context;
		Dlna.listener = listener;
	}

	/**
	 * 开启dlna，可选择性地开启dmc,dms和同时开启dmc与dms
	 * 
	 * @param type
	 *            :Dlna.DMC\DMS\DMC_DMS
	 * @param context
	 * @return
	 */
	public static void start(int type) {
		ServiceManager.getInstance().start(type);
	}

	/**
	 * 关闭dlna服务
	 */
	public static void stop() {
		ServiceManager.getInstance().stop();
	}

	public static ServiceState getState() {
		return ServiceManager.getInstance().getState();
	}

	/**
	 * DMS相关设置
	 */
	public static class DMS {

		public static void setShareEnable(boolean enable) {
			MediaStoreContent.getInstance().setShareEnable(enable);
		}

		public static boolean addShareFilePath(String path) {
			return MediaStoreContent.getInstance().addShareFilePath(path);
		}

		public static boolean removeShareFilePath(String path) {
			return MediaStoreContent.getInstance().removeShareFilePath(path);
		}

		public static boolean removeAllShareFile() {
			return MediaStoreContent.getInstance().removeAllShareFile();
		}

		public static void shareExternalPhotos() {
			MediaStoreContent.getInstance().shareExternalPhotos();
		}

		public static void shareExternalMusics() {
			MediaStoreContent.getInstance().shareExternalMusics();
		}

		public static void shareExternalMovies() {
			MediaStoreContent.getInstance().shareExternalMovies();
		}

		public static void shareAllExternalMedia() {
			MediaStoreContent.getInstance().shareAllExternalMedia();
		}
	}

	/**
	 * 使用默认方式搜索机顶盒
	 */
	public static DeviceManager getDeviceManager() {
		return DeviceManager.getInstance();
	}

	public interface OnDlnaStateChangeListener {
		public void onStateChange(ServiceState state);
	}

}
