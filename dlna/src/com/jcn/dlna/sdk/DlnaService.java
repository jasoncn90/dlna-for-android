package com.jcn.dlna.sdk;

import java.util.logging.Logger;

import org.teleal.cling.android.AndroidUpnpServiceConfiguration;

import android.net.wifi.WifiManager;

import com.jcn.dlna.sdk.ServiceManager.ServiceState;
import com.jcn.dlna.sdk.cling.AndroidUpnpServiceImpl;

/**
 * 优化cling在Android系统上的使用，降低cpu的负荷
 * 
 */
public class DlnaService extends AndroidUpnpServiceImpl {

	private static final Logger log = Logger.getLogger(DlnaService.class
			.getSimpleName());

	@Override
	protected AndroidUpnpServiceConfiguration createConfiguration(
			WifiManager wifiManager) {
		return new AndroidUpnpServiceConfiguration(wifiManager) {
			@Override
			public int getRegistryMaintenanceIntervalMillis() {
				return 7000;
			}
		};
	}

	@Override
	public void onDestroy() {
		log.info(">>> Shutting down UPnP service...");
		try {
			super.onDestroy();
		} catch (Exception ex) {
			log.info("<<< UPnP service shutdown completed");
		} finally {
			ServiceManager.getInstance().setState(ServiceState.CLOSED);
		}
	}
}
