package com.jcn.dlna.sdk.dmc;

import java.util.HashSet;
import java.util.Set;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.message.header.UDADeviceTypeHeader;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;

import com.jcn.dlna.sdk.ActionHandler;
import com.jcn.dlna.sdk.ServiceManager;

/**
 * 设备管理器
 */
@SuppressWarnings("rawtypes")
public class DeviceManager {

	private static DeviceManager instance;
	private DeviceListRegistryListener listener;
	private Set<Device> deviceSet;

	private DeviceManager() {
		listener = new DeviceListRegistryListener();
		deviceSet = new HashSet<Device>();
		service = ServiceManager.getService();
	}

	private OnSearchDeviceCallback callback;
	private AndroidUpnpService service;

	public static DeviceManager getInstance() {
		if (instance == null) {
			instance = new DeviceManager();
		}
		return instance;
	}

	public void destory() {
		if (deviceSet != null) {
			deviceSet.clear();
		}
		if (service != null) {
			service.getRegistry().removeListener(listener);
		}
		service = null;
	}

	public void clearDevices() {
		if (service != null) {
			service.getRegistry().removeAllRemoteDevices();
		}
	}

	public void searchDevices(final OnSearchDeviceCallback callback) {
		if (ActionHandler.isAlive())
			ActionHandler.getHandler().post(new Runnable() {

				@Override
				public void run() {
					if (service == null) {
						service = ServiceManager.getService();
					}
					service.getRegistry().addListener(listener);
					DeviceManager.this.callback = callback;
					UDADeviceType udaType = new UDADeviceType("MediaRenderer");
					service.getControlPoint().search(
							new UDADeviceTypeHeader(udaType));
				}
			});
	}

	public interface OnSearchDeviceCallback {

		public void onDeviceAdd(DmrDevice device);

		public void onDeviceDown(DmrDevice device);
	}

	private class DeviceListRegistryListener extends DefaultRegistryListener {

		@Override
		public void deviceAdded(Registry registry, Device device) {
			super.deviceAdded(registry, device);
			if (device.getType().toString().contains("MediaRenderer")
					&& callback != null) {
				if (deviceSet.add(device))
					callback.onDeviceAdd(new DmrDevice(device));
			}
		}

		@Override
		public void deviceRemoved(Registry registry, Device device) {
			super.deviceRemoved(registry, device);
			if (device.getType().toString().contains("MediaRenderer")
					&& callback != null) {
				deviceSet.remove(device);
				callback.onDeviceDown(new DmrDevice(device));
			}
		}
	}
}
