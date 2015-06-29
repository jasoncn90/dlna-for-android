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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.net.wifi.WifiManager;

import com.jcn.dlna.sdk.Utils;
import com.jcn.dlna.sdk.WifiReceiver;

/**
 * @author Christian Bauer
 */
public class HttpServer {

	final private static Logger log = Logger.getLogger(HttpServer.class
			.getName());

	final int listenPort;
	final HttpRequestHandlerRegistry handlerRegistry;
	final HttpParams params;
	final WifiManager wifiManager;
	private boolean isStarted = false;

	ListenerThread listenerThread;

	private static HttpServer instance;

	private HttpServer(WifiManager wifiManager) {
		this.wifiManager = wifiManager;
		this.listenPort = 0;

		this.handlerRegistry = new HttpRequestHandlerRegistry();

		this.params = new BasicHttpParams();
		this.params
				.setParameter(CoreProtocolPNames.ORIGIN_SERVER,
						"4thLineAndroidHttpServer/1.0")
				.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
				.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
						8 * 1024)
				.setBooleanParameter(
						CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
				.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);

		startServer();
	}

	public static void init(WifiManager wifiManager) {
		if (instance == null) {
			instance = new HttpServer(wifiManager);
		}
	}

	public static HttpServer getInstance() {
		return instance;
	}

	public InetAddress getLocalInetAddress() {
		return Utils.getWifiInetAddress(wifiManager);
	}

	public int getListenPort() {
		return listenPort;
	}

	public int getLocalPort() {
		return listenerThread != null ? listenerThread.getActualListenPort()
				: -1;
	}

	public HttpParams getParams() {
		return params;
	}

	public HttpRequestHandlerRegistry getHandlerRegistry() {
		return handlerRegistry;
	}

	synchronized public void startServer() {
		if (isStarted) {
			return;
		}
		InetAddress localInetAddress = getLocalInetAddress();
		if (localInetAddress == null
				|| !WifiReceiver.getInstance().isConnected()) {
			log.severe("Can't start server, can't find local network interface IP address to bind to");
			return;
		}
		try {
			listenerThread = new ListenerThread(localInetAddress,
					getListenPort(), getParams(), getHandlerRegistry());
			listenerThread.start(); // Don't need non-daemon status, we are on
									// Android
			log.info("HTTP server start finish");
			log.info("address-->" + localInetAddress.getHostAddress() + ":"
					+ getLocalPort());
			isStarted = true;
		} catch (IOException ex) {
			log.severe("Can't start server, error binding listener socket: "
					+ ex);
		}
	}

	synchronized public void stopServer() {
		if (!isStarted) {
			return;
		}
		if (listenerThread != null) {
			log.info("Stopping HTTP server...");
			isStarted = false;
			listenerThread.stopListening();
		}
	}

	static class ListenerThread extends Thread {

		private volatile boolean stopped = false;
		final HttpParams params;
		final ServerSocket serverSocket;
		final HttpService httpService;

		public ListenerThread(InetAddress address, int port, HttpParams params,
				HttpRequestHandlerRegistry handlerRegistry) throws IOException {
			this.params = params;
			this.serverSocket = new ServerSocket(port, 0, address);

			BasicHttpProcessor httpproc = new BasicHttpProcessor();
			httpproc.addInterceptor(new ResponseDate());
			httpproc.addInterceptor(new ResponseServer());
			httpproc.addInterceptor(new ResponseContent());
			httpproc.addInterceptor(new ResponseConnControl());

			this.httpService = new HttpService(httpproc,
					new DefaultConnectionReuseStrategy(),
					new DefaultHttpResponseFactory());
			this.httpService.setParams(params);
			this.httpService.setHandlerResolver(handlerRegistry);
		}

		public int getActualListenPort() {
			return this.serverSocket.getLocalPort();
		}

		@Override
		public void run() {
			log.fine("Starting listener thread on local address and port; "
					+ this.serverSocket.getLocalSocketAddress());
			while (!stopped) {
				try {

					Socket clientSocket = serverSocket.accept(); // Block until
																	// we have a
																	// connection
					DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
					log.finer("Incoming connection from "
							+ clientSocket.getInetAddress());
					conn.bind(clientSocket, params);

					Thread t = new WorkerThread(httpService, conn);
					t.setDaemon(true);
					t.start();

				} catch (InterruptedIOException ex) {
					log.fine("I/O has been interrupted, stopping receiving loop, bytes transfered: "
							+ ex.bytesTransferred);
					break;
				} catch (SocketException ex) {
					if (!stopped) {
						// That's not good, could be anything
						log.fine("Exception using server socket: "
								+ ex.getMessage());
					} else {
						// Well, it's just been stopped so that's totally fine
						// and expected
					}
					break;
				} catch (IOException ex) {
					log.severe("I/O error initializing worker thread, aborting: "
							+ ex);
					break;
				}
			}
		}

		public void stopListening() {
			try {
				stopped = true;
				if (!serverSocket.isClosed()) {
					log.fine("Closing server socket");
					serverSocket.close();
				}
			} catch (Exception ex) {
				log.info("Exception closing server socket: " + ex.getMessage());
			}
		}
	}

	static class WorkerThread extends Thread {

		final HttpService httpservice;
		final HttpServerConnection conn;

		public WorkerThread(final HttpService httpservice,
				final HttpServerConnection conn) {
			super();
			this.httpservice = httpservice;
			this.conn = conn;
		}

		@Override
		public void run() {
			HttpContext context = new BasicHttpContext(null);
			try {
				while (!Thread.interrupted() && this.conn.isOpen()) {
					this.httpservice.handleRequest(this.conn, context);
				}
			} catch (ConnectionClosedException ex) {
				log.fine("Client closed connection");
			} catch (SocketTimeoutException ex) {
				log.fine("Server-side closed socket (this is 'normal' behavior of Apache HTTP Core!): "
						+ ex.getMessage());
			} catch (IOException ex) {
				// Could be a peer connection reset, no warning
				log.fine("I/O exception during HTTP request processing: "
						+ ex.getMessage());
			} catch (HttpException ex) {
				throw new RuntimeException("Request malformed: "
						+ ex.getMessage(), ex);
			} finally {
				try {
					conn.shutdown();
				} catch (IOException ex) {
					log.fine("Error closing connection: " + ex.getMessage());
				}
			}
		}

	}

}
