/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jcn.dlna.sdk.dms.httpserver;

import org.apache.http.protocol.HttpRequestHandler;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;

/**
 * @author Christian Bauer
 */
public class HttpServerServiceImpl extends Service {

	protected Binder binder = new Binder();

	protected class Binder extends android.os.Binder implements
			HttpServerService {
		@Override
		public int getLocalPort() {
			return httpServer.getLocalPort();
		}

		@Override
		public void addHandler(String pattern, HttpRequestHandler handler) {
			httpServer.getHandlerRegistry().register(pattern, handler);
		}

		@Override
		public void removeHandler(String pattern) {
			httpServer.getHandlerRegistry().unregister(pattern);
		}

	}

	protected HttpServer httpServer;

	@Override
	public void onCreate() {
		super.onCreate();
		final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		HttpServer.init(wifiManager);
		httpServer = HttpServer.getInstance();
	}

	@Override
	public void onDestroy() {
		httpServer.stopServer();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

}
