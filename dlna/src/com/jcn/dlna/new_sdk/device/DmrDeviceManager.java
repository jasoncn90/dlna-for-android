package com.jcn.dlna.new_sdk.device;

import java.util.HashSet;
import java.util.Set;

import org.teleal.cling.model.meta.Device;

public class DmrDeviceManager extends DeviceManager {

	private OnSearchDmrDeviceListener listener;
	private Set<DmrDevice> dmrDevices;

	public DmrDeviceManager() {
		callback = new OnReceiveDeviceSearchResult();
		dmrDevices = new HashSet<DmrDevice>();
	}

	public void searchDmrDevices(OnSearchDmrDeviceListener listener) {
		this.listener = listener;
		dmrDevices.clear();
		super.search();
	}

	public Set<DmrDevice> getDevices() {
		return dmrDevices;
	}

	public interface OnSearchDmrDeviceListener {
		public void onDeviceAdd(DmrDevice device);

		public void onDeviceRemove(DmrDevice device);
	}

	private class OnReceiveDeviceSearchResult extends OnSearchDeviceCallback {

		@Override
		public void onDeviceAdd(Device device) {
			if (device.getType().toString().contains("MediaRenderer")) {
				DmrDevice dmrDevice = new DmrDevice(device);
				if (dmrDevices.add(dmrDevice)) {
					listener.onDeviceAdd(dmrDevice);
				}
			}
		}

		@Override
		public void onDeviceRemove(Device device) {
			if (device.getType().toString().contains("MediaRenderer")) {
				for (DmrDevice dmrDevice : dmrDevices) {
					if (device.getIdentity().getUdn().toString()
							.equals(dmrDevice.getUdn())) {
						dmrDevices.remove(dmrDevice);
						listener.onDeviceRemove(dmrDevice);
					}
				}
			}
		}

	}

}
