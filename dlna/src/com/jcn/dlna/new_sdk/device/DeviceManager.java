package com.jcn.dlna.new_sdk.device;

import java.util.logging.Logger;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;

import com.jcn.dlna.new_sdk.DlnaService;

public abstract class DeviceManager {

	private static final Logger log = Logger.getLogger(DeviceManager.class
			.getName());
	private DeviceListRegistryListener listener;
	public OnSearchDeviceCallback callback;

	public DeviceManager() {
		listener = new DeviceListRegistryListener();
	}

	protected void search() {
		AndroidUpnpService service = DlnaService.getInstance().getService();
		service.getRegistry().addListener(listener);
		service.getControlPoint().search();
	}

	public void removeAllDevices() {
		AndroidUpnpService service = DlnaService.getInstance().getService();
		service.getRegistry().removeAllRemoteDevices();
		service.getRegistry().removeAllLocalDevices();
	}

	public abstract class OnSearchDeviceCallback {

		public abstract void onDeviceAdd(Device device);

		public abstract void onDeviceRemove(Device device);
	}

	@SuppressWarnings("rawtypes")
	private class DeviceListRegistryListener extends DefaultRegistryListener {

		@Override
		public void deviceAdded(Registry registry, Device device) {
			super.deviceAdded(registry, device);
			if (callback != null) {
				callback.onDeviceAdd(device);
			}
		}

		@Override
		public void deviceRemoved(Registry registry, Device device) {
			super.deviceRemoved(registry, device);
			if (callback != null) {
				callback.onDeviceRemove(device);
			}
		}
	}
}
