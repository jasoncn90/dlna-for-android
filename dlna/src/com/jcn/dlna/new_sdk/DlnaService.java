package com.jcn.dlna.new_sdk;

import java.util.logging.Logger;

import org.teleal.cling.android.AndroidUpnpService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jcn.dlna.new_sdk.cling.UpnpServiceImpl;

public class DlnaService {

	private static final Logger log = Logger.getLogger(DlnaService.class
			.getName());

	private static DlnaService instance;
	private AndroidUpnpService upnpService;

	private DlnaService() {
	}

	public static DlnaService getInstance() {
		if (instance == null) {
			instance = new DlnaService();
		}
		return instance;
	}

	public AndroidUpnpService getService() {
		if (upnpService == null) {
			throw new IllegalStateException("haven't start dlna service");
		}
		return upnpService;
	}

	public void bind(Context context) {
		context.bindService(new Intent(context, UpnpServiceImpl.class), conn,
				Context.BIND_AUTO_CREATE);
	}

	public void unbind(Context context) {
		context.unbindService(conn);
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			upnpService = null;
			log.info("dlnaService disconnected");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			upnpService = (AndroidUpnpService) service;
			log.info("dlnaService connected");
		}
	};
}
