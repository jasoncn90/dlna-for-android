package com.jcn.dlna.sdk;

import java.util.logging.Logger;

import org.teleal.cling.android.AndroidUpnpService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.jcn.dlna.sdk.Dlna.OnDlnaStateChangeListener;
import com.jcn.dlna.sdk.dmc.DeviceManager;
import com.jcn.dlna.sdk.dms.httpserver.HttpServerManager;
import com.jcn.dlna.sdk.dms.mediaserver.MediaServerService;

/**
 * dlna服务管理器
 */
public class ServiceManager {

	public static enum ServiceState {
		CLOSED, DMC_OPENED, DMS_OPENED, DMC_DMS_OPENED
	}

	private static final Logger log = Logger.getLogger(ServiceManager.class
			.getSimpleName());

	private AndroidUpnpService upnpService;
	private ServiceConnection serviceConnection;
	private Context context;
	private Context appContext;
	private ServiceState state;
	private OnDlnaStateChangeListener listener;
	private boolean openDmsOnly = false;

	private static ServiceManager instance;

	private ServiceManager() {
		context = Dlna.getContext();
		appContext = Dlna.getApplicationContext();
		state = ServiceState.CLOSED;
		listener = Dlna.getListener();
	}

	public void setState(ServiceState state) {
		if (state == ServiceState.DMS_OPENED) {
			if (!openDmsOnly) {
				state = ServiceState.DMC_DMS_OPENED;
			}
		}
		this.state = state;
		dispatchOnStateChange(state);
	}

	public ServiceState getState() {
		return state;
	}

	private void dispatchOnStateChange(ServiceState state) {
		if (listener != null) {
			listener.onStateChange(state);
		}
	}

	public static ServiceManager getInstance() {
		if (instance == null) {
			instance = new ServiceManager();
		}
		return instance;
	}

	private ServiceConnection dmcServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			if (service instanceof AndroidUpnpService) {
				upnpService = (AndroidUpnpService) service;
			}
			log.info("dlnaService bind success");
			WifiReceiver.getInstance().register(context);
			HttpServerManager.getInstance().startHttpServer(context);
			setState(ServiceState.DMC_OPENED);
			ActionHandler.resume();
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			log.info("dlnaService disconnected");
			upnpService = null;
		}
	};

	private ServiceConnection dmsServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			upnpService = (AndroidUpnpService) service;
			log.info("dlnaService bind success");
			WifiReceiver.getInstance().register(context);
			HttpServerManager.getInstance().startHttpServer(context);
			context.startService(new Intent(context, MediaServerService.class));
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			log.info("dlnaService disconnected");
			upnpService = null;
		}
	};

	public static AndroidUpnpService getService() {
		return getInstance().upnpService;
	}

	public void stop() {
		if (instance != null) {
			synchronized (instance) {
				Log.d("ServiceManager", "stop dlna begin!");
				state = ServiceState.CLOSED;
				if (ActionHandler.isAlive()) {
					DeviceManager.getInstance().destory();
					WifiReceiver.getInstance().unregister(context);
					HttpServerManager.getInstance().stopHttpServer(context);
					context.stopService(new Intent(context,
							MediaServerService.class));
					try {
						if (serviceConnection != null) {
							appContext.unbindService(serviceConnection);
							serviceConnection = null;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					ActionHandler.stop();
				}
				Log.d("ServiceManager", "stop dlna end!");
			}
		}
	}

	public boolean isDmcOpen() {
		if (state == ServiceState.DMC_OPENED
				|| state == ServiceState.DMC_DMS_OPENED) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isDmsOpen() {
		if (state == ServiceState.DMS_OPENED
				|| state == ServiceState.DMC_DMS_OPENED) {
			return true;
		} else {
			return false;
		}
	}

	public void start(final int type) {
		if (instance != null) {
			synchronized (instance) {
				Log.d("ServiceManager", "start dlna begin!");
				if (type == Dlna.TYPE_DMC && state == ServiceState.DMC_OPENED)
					return;
				if (type == Dlna.TYPE_DMS && state == ServiceState.DMS_OPENED)
					return;
				if (type == Dlna.TYPE_DMC_DMS
						&& state == ServiceState.DMC_DMS_OPENED)
					return;
				Log.d("ServiceManager", "on dlna service start!");
				ActionHandler.init();
				ActionHandler.getHandler().post(new Runnable() {
					@Override
					public void run() {
						try {
							if (serviceConnection != null) {
								appContext.unbindService(serviceConnection);
								serviceConnection = null;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						switch (type) {
						case Dlna.TYPE_DMC:
							serviceConnection = dmcServiceConnection;
							appContext.bindService(new Intent(context,
									DlnaService.class), serviceConnection,
									Context.BIND_AUTO_CREATE);
							break;
						case Dlna.TYPE_DMS:
							openDmsOnly = true;
							try {
								context.stopService(new Intent(context,
										MediaServerService.class));
							} catch (Exception e) {
							}
							serviceConnection = dmsServiceConnection;
							appContext.bindService(new Intent(context,
									DlnaService.class), serviceConnection,
									Context.BIND_AUTO_CREATE);
							break;
						case Dlna.TYPE_DMC_DMS:
							openDmsOnly = false;
							try {
								context.stopService(new Intent(context,
										MediaServerService.class));
							} catch (Exception e) {
							}
							serviceConnection = dmsServiceConnection;
							appContext.bindService(new Intent(context,
									DlnaService.class), serviceConnection,
									Context.BIND_AUTO_CREATE);
							break;
						default:
							break;
						}
						ActionHandler.pause();
					}
				});
				Log.d("ServiceManager", "start dlna end!");
			}
		}
	}
}
