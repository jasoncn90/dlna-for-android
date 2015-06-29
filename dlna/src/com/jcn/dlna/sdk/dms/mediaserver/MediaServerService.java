package com.jcn.dlna.sdk.dms.mediaserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.LocalDevice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.jcn.dlna.sdk.ActionHandler;
import com.jcn.dlna.sdk.ServiceManager;
import com.jcn.dlna.sdk.ServiceManager.ServiceState;

public class MediaServerService extends Service {

	private static final Logger log = Logger.getLogger(MediaServerService.class
			.getSimpleName());

	protected Binder binder = new Binder();

	private MediaServer server;
	private AndroidUpnpService upnpService;

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		log.info("starting http server and add device");
		// ´´½¨ÐéÄâdevice
		server = new MediaServer();
		upnpService = ServiceManager.getService();
		LocalDevice mediaServerDevice = upnpService.getRegistry()
				.getLocalDevice(server.getUdn(), true);
		if (mediaServerDevice == null) {
			try {
				mediaServerDevice = server.createDevice();
				upnpService.getRegistry().addDevice(mediaServerDevice);
				ServiceManager.getInstance().setState(ServiceState.DMS_OPENED);
				ActionHandler.resume();
			} catch (Exception ex) {
				log.log(Level.SEVERE,
						"Creating or registering media server device failed",
						ex);
				ServiceManager.getInstance().setState(ServiceState.CLOSED);
				ActionHandler.resume();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		log.info("removing device");
		upnpService.getRegistry().removeDevice(server.getUdn());
	}

}
