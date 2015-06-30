package com.jcn.dlna.new_sdk.cling;

import java.util.logging.Logger;

import org.teleal.cling.android.AndroidUpnpServiceConfiguration;

import android.net.wifi.WifiManager;

import com.jcn.dlna.sdk.ServiceManager;
import com.jcn.dlna.sdk.ServiceManager.ServiceState;

/**
 * @author Jason
 *
 *         this is a optimized AndroidUpnpServiceImpl for android ,decrease the
 *         cpu usage and clear devices cache when shut down .
 */
public class UpnpServiceImpl extends AndroidUpnpServiceImpl {

	private static final Logger log = Logger.getLogger(UpnpServiceImpl.class
			.getName());

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
