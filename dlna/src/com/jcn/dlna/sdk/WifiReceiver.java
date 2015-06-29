package com.jcn.dlna.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.jcn.dlna.sdk.dms.httpserver.HttpServer;

/**
 * 用于处理wifi连接变更的事件
 */
public class WifiReceiver {

	private MyWifiReceiver receiver = null;
	private boolean isConnected = false;

	private WifiReceiver() {
	}

	private static WifiReceiver instance;

	public static WifiReceiver getInstance() {
		if (instance == null) {
			instance = new WifiReceiver();
		}
		return instance;
	}

	public void register(Context context) {
		isConnected = checkWifiIsConnected(context);
		if (receiver == null) {
			receiver = new MyWifiReceiver();
			context.registerReceiver(receiver, new IntentFilter(
					"android.net.conn.CONNECTIVITY_CHANGE"));
		}
	}

	public void unregister(Context context) {
		if (receiver != null) {
			context.unregisterReceiver(receiver);
			receiver = null;
		}
	}

	public boolean isConnected() {
		return isConnected;
	}

	private class MyWifiReceiver extends BroadcastReceiver {
		@Override
		synchronized public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(
					ConnectivityManager.CONNECTIVITY_ACTION))
				return;
			if (HttpServer.getInstance() == null)
				return;
			if (!checkWifiIsConnected(context)) {
				isConnected = false;
				HttpServer.getInstance().stopServer();
			} else {
				isConnected = true;
				HttpServer.getInstance().startServer();
			}
		}
	}

	private boolean checkWifiIsConnected(Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (!wifiInfo.isConnected()) {
			return false;
		} else {
			return true;
		}
	}

}
