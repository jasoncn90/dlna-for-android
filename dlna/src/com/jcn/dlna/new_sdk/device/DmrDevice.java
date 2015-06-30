package com.jcn.dlna.new_sdk.device;

import java.io.UnsupportedEncodingException;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UDAServiceType;

import android.text.TextUtils;

public class DmrDevice implements Comparable<DmrDevice> {

	private Device<?, ?, ?> device;
	private String friendlyName = "";

	public static enum PlayState {
		PLAYING, LOADING, STOPPED, PAUSED, FALSE
	}

	protected DmrDevice(Device<?, ?, ?> device) {
		this.device = device;
		String friendlyName = device.getDetails().getFriendlyName();
		if (!TextUtils.isEmpty(friendlyName)) {
			try {
				byte[] bytes = new byte[friendlyName.length()];
				for (int i = 0; i < bytes.length; ++i) {
					bytes[i] = (byte) (friendlyName.charAt(i) & 0x00FF);
				}
				this.friendlyName = new String(bytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	public String getName() {
		return friendlyName;
	}

	public String getType() {
		return device.getType().toString();
	}

	protected Service<?, ?> findService(UDAServiceType udaServiceType) {
		return device.findService(udaServiceType);
	}

	public String getUdn() {
		return device.getIdentity().getUdn().toString();
	}

	public String getIp() {
		String http = "http://";
		int firstIndex = device.getIdentity().toString().indexOf(http)
				+ http.length();
		String remain = device.getIdentity().toString().substring(firstIndex);
		String ip = remain.substring(0, remain.indexOf("/"));
		int secondIndex = ip.indexOf(":");
		String finalIp = ip.substring(0, secondIndex);
		return finalIp;
	}

	public interface OnDeviceStateChangeListener {

		public void onPlayStateChanged(PlayState state);

		public void onPositionChanged(long progress, long duration);

		public void onVolumeChanged(int volume);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(DmrDevice another) {
		if (another == null) {
			return 1;
		}
		if (TextUtils.isEmpty(this.getUdn())) {
			if (TextUtils.isEmpty(another.getUdn())) {
				return 0;
			} else {
				return -1;
			}
		}
		if (TextUtils.isEmpty(another.getUdn())) {
			return 1;
		}
		return this.getUdn().compareTo(another.getUdn());
	}

}
