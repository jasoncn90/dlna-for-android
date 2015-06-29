package com.jcn.dlna.sdk;

import java.util.logging.Logger;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * 对于upnpService的所有操作需要在此类中处理，通过handler一个个地发送消息
 */
public class ActionHandler {

	private static final Logger log = Logger.getLogger(ActionHandler.class
			.getSimpleName());
	public static final String THREADNAME = "dlna action thread";
	private static HandlerThread handlerThread;
	private static Handler handler;

	public static void init() {
		if (handler == null || handlerThread == null
				|| !handlerThread.isAlive()) {
			handlerThread = new HandlerThread(THREADNAME);
			handlerThread.start();
			handler = new Handler(handlerThread.getLooper());
		} else {
			log.severe("dlna action thread is alive,ignore init");
		}
	}

	public static Handler getHandler() {
		return handler;
	}

	public static boolean isAlive() {
		if (handler == null || handlerThread == null
				|| !handlerThread.isAlive()) {
			return false;
		} else {
			return true;
		}
	}

	public static void pause() {
		log.severe("pause");
		synchronized (handlerThread) {
			try {
				handlerThread.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void resume() {
		log.severe("resume");
		synchronized (handlerThread) {
			handlerThread.notify();
		}
	}

	public static void pause(final long millis) {
		synchronized (handlerThread) {
			try {
				handlerThread.wait(millis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void clear() {
		handler.getLooper().quit();
	}

	public static void stop() {
		synchronized (handlerThread) {
			try {
				handlerThread.wait(500);
				handler.getLooper().quit();
				handlerThread.interrupt();
				handler = null;
				handlerThread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
