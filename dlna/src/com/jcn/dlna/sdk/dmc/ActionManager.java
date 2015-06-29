package com.jcn.dlna.sdk.dmc;

import java.lang.Thread.State;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UDAServiceType;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.jcn.dlna.sdk.MediaInfo;
import com.jcn.dlna.sdk.ServiceManager;
import com.jcn.dlna.sdk.WifiReceiver;
import com.jcn.dlna.sdk.dmc.DmrDevice.OnDeviceStateChangeListener;
import com.jcn.dlna.sdk.dmc.DmrDevice.PlayState;
import com.jcn.dlna.sdk.dmc.avtransport.GetPositionInfoImpl;
import com.jcn.dlna.sdk.dmc.avtransport.GetTransportInfoImpl;
import com.jcn.dlna.sdk.dmc.avtransport.PauseImpl;
import com.jcn.dlna.sdk.dmc.avtransport.PlayImpl;
import com.jcn.dlna.sdk.dmc.avtransport.SeekImpl;
import com.jcn.dlna.sdk.dmc.avtransport.SetAVTransportURIImpl;
import com.jcn.dlna.sdk.dmc.avtransport.StopImpl;
import com.jcn.dlna.sdk.dmc.renderingcontrol.GetMuteImpl;
import com.jcn.dlna.sdk.dmc.renderingcontrol.GetVolumeImpl;
import com.jcn.dlna.sdk.dmc.renderingcontrol.SetMuteImpl;
import com.jcn.dlna.sdk.dmc.renderingcontrol.SetVolumeImpl;

/**
 * 管理对机顶盒进行的操作
 */
public class ActionManager {
	private static final Logger log = Logger.getLogger(ActionManager.class
			.getSimpleName());
	private static ActionManager instance;

	private static final UDAServiceType AV_TRANSPORT_TYPE = new UDAServiceType(
			"AVTransport");
	private static final UDAServiceType RENDERING_CONTROL_TYPE = new UDAServiceType(
			"RenderingControl");
	private AndroidUpnpService service;
	private MediaInfo pushingMedia;

	private Map<String, SyncHandler> handlers;

	private ActionManager() {
		service = ServiceManager.getService();
		handlers = new HashMap<String, SyncHandler>();
	}

	public static ActionManager getInstance() {
		if (instance == null) {
			instance = new ActionManager();
		}
		return instance;
	}

	public boolean push(final DmrDevice device, final MediaInfo media,
			Handler handler) {
		if (getCurrentAction(device, handler) != PlayState.STOPPED) {
			boolean stopresult = stop(device, handler);
			log.warning("play state is not stopped ,stop the play->"
					+ stopresult);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
		if (!checkActionIsPrepared())
			return false;
		pushingMedia = media;
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			return false;
		}
		final SetAVTransportURIImpl action = new SetAVTransportURIImpl(
				dlnaService, media.getPushUrl().trim(), media.getMetaData(),
				Thread.currentThread());
		handler.post(new Runnable() {
			@Override
			public void run() {
				log.warning("action for service to execute->"
						+ action.toString());
				service.getControlPoint().execute(action);
			}
		});
		blockCurrentThread();
		if (action.getResult() == true) {
			return resume(device, handler);
		}
		return action.getResult();
	}

	public boolean push(final DmrDevice device, final MediaInfo media,
			final long position, final Handler handler) {
		if (!push(device, media, handler))
			return false;
		try {
			boolean success = false;
			for (int i = 0; i < 15; i++) {
				PlayState state = getCurrentAction(device, handler);
				if (state == PlayState.PLAYING) {
					Thread.sleep(1200);
					success = seek(device, position, handler);
					log.warning("success to seek=" + success);
					break;
				}
				Thread.sleep(1200);
			}
			return success;
		} catch (Exception e) {
			return false;
		}
	}

	public long getPositionInfo(final DmrDevice device, Handler handler) {
		if (!checkActionIsPrepared())
			return -1;
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			return -1;
		}
		final GetPositionInfoImpl action = new GetPositionInfoImpl(dlnaService,
				Thread.currentThread());
		handler.post(new Runnable() {
			@Override
			public void run() {
				service.getControlPoint().execute(action);
			}
		});
		blockCurrentThread();
		return action.getPosition();
	}

	public PositionInfo getPositionInfo1(final DmrDevice device, Handler handler) {
		if (!checkActionIsPrepared())
			return null;
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			return null;
		}
		final GetPositionInfoImpl action = new GetPositionInfoImpl(dlnaService,
				Thread.currentThread());
		handler.post(new Runnable() {
			@Override
			public void run() {
				service.getControlPoint().execute(action);
			}
		});
		blockCurrentThread();
		PositionInfo result = new PositionInfo();
		result.duration = action.getDuration();
		result.position = action.getPosition();
		return result;
	}

	private static class PositionInfo {
		long position;
		long duration;
	}

	public boolean resume(final DmrDevice device, Handler handler) {
		if (!checkActionIsPrepared())
			return false;
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			return false;
		}
		final PlayImpl action = new PlayImpl(dlnaService,
				Thread.currentThread());
		handler.post(new Runnable() {
			@Override
			public void run() {
				service.getControlPoint().execute(action);
			}
		});
		blockCurrentThread();
		return action.getResult();
	}

	public boolean pause(final DmrDevice device, Handler handler) {
		if (!checkActionIsPrepared())
			return false;
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			return false;
		}
		final PauseImpl action = new PauseImpl(dlnaService,
				Thread.currentThread());
		handler.post(new Runnable() {
			@Override
			public void run() {
				service.getControlPoint().execute(action);
			}
		});
		blockCurrentThread();
		return action.getResult();
	}

	public boolean stop(final DmrDevice device, Handler handler) {
		if (!checkActionIsPrepared())
			return false;
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			return false;
		}
		final StopImpl action = new StopImpl(dlnaService,
				Thread.currentThread());
		handler.post(new Runnable() {
			@Override
			public void run() {
				service.getControlPoint().execute(action);
			}
		});
		blockCurrentThread();
		return action.getResult();
	}

	public boolean seek(final DmrDevice device, final long millisecond,
			Handler handler) {
		if (!checkActionIsPrepared())
			return false;
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			return false;
		}
		final SeekImpl action = new SeekImpl(dlnaService,
				parseMillisecond(millisecond), Thread.currentThread());
		handler.post(new Runnable() {
			@Override
			public void run() {
				service.getControlPoint().execute(action);
			}
		});
		blockCurrentThread();
		return action.getResult();
	}

	public PlayState getCurrentAction(final DmrDevice device, Handler handler) {
		if (!checkActionIsPrepared())
			return PlayState.FALSE;
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			return PlayState.FALSE;
		}
		final GetTransportInfoImpl action = new GetTransportInfoImpl(
				dlnaService, Thread.currentThread());
		handler.post(new Runnable() {
			@Override
			public void run() {
				service.getControlPoint().execute(action);
			}
		});
		blockCurrentThread();
		return action.getResult();
	}

	public boolean setVolume(final DmrDevice device, final long volume,
			Handler handler) {
		if (!checkActionIsPrepared())
			return false;
		Service<?, ?> dlnaService = device.findService(RENDERING_CONTROL_TYPE);
		if (dlnaService == null) {
			return false;
		}
		final SetVolumeImpl action = new SetVolumeImpl(dlnaService, volume,
				Thread.currentThread());
		handler.post(new Runnable() {
			@Override
			public void run() {
				service.getControlPoint().execute(action);
			}
		});
		blockCurrentThread();
		return action.getResult();
	}

	public int getVolume(final DmrDevice device, Handler handler) {
		if (!checkActionIsPrepared())
			return -1;
		Service<?, ?> dlnaService = device.findService(RENDERING_CONTROL_TYPE);
		if (dlnaService == null) {
			return -1;
		}
		try {
			final GetVolumeImpl action = new GetVolumeImpl(dlnaService,
					Thread.currentThread());
			handler.post(new Runnable() {
				@Override
				public void run() {
					service.getControlPoint().execute(action);
				}
			});
			blockCurrentThread();
			return action.getResult();
		} catch (Exception e) {
			return -1;
		}
	}

	public boolean setMute(final DmrDevice device, final boolean desiredMute,
			Handler handler) {
		if (!checkActionIsPrepared())
			return false;
		Service<?, ?> dlnaService = device.findService(RENDERING_CONTROL_TYPE);
		if (dlnaService == null) {
			return false;
		}
		final SetMuteImpl action = new SetMuteImpl(dlnaService, desiredMute,
				Thread.currentThread());
		handler.post(new Runnable() {

			@Override
			public void run() {
				service.getControlPoint().execute(action);
			}
		});
		blockCurrentThread();
		return action.getResult();
	}

	public boolean getMute(final DmrDevice device, Handler handler) {
		if (!checkActionIsPrepared())
			return false;
		Service<?, ?> dlnaService = device.findService(RENDERING_CONTROL_TYPE);
		if (dlnaService == null) {
			return false;
		}
		final GetMuteImpl action = new GetMuteImpl(dlnaService,
				Thread.currentThread());
		handler.post(new Runnable() {
			@Override
			public void run() {
				service.getControlPoint().execute(action);
			}
		});
		blockCurrentThread();
		return action.getResult();
	}

	public void startSync(final DmrDevice device,
			final OnDeviceStateChangeListener listener) {
		if (handlers.containsKey(device.getUdn())) {
			return;
		}
		HandlerThread thread = new HandlerThread(String.valueOf(System
				.currentTimeMillis()));
		thread.start();
		Handler actionHandler = new Handler(thread.getLooper());
		// 如果当前线程没有Looper，需要创建Looper对象
		if (Looper.myLooper() == null) {
			Looper.prepare();
			SyncHandler handler = new SyncHandler(Looper.myLooper(), device,
					listener, actionHandler);
			handlers.put(device.getUdn(), handler);
			handler.start();
			Looper.loop();
		} else {
			SyncHandler handler = new SyncHandler(Looper.myLooper(), device,
					listener, actionHandler);
			handlers.put(device.getUdn(), handler);
			handler.start();
		}
	}

	public void stopSync(final DmrDevice device) {
		SyncHandler handler = handlers.get(device.getUdn());
		if (handler == null)
			return;
		handler.stop();
		handlers.remove(device.getUdn());
	}

	public boolean isSyncing(final DmrDevice device) {
		return handlers.containsKey(device.getUdn());
	}

	private static class SyncHandler extends Handler {

		private DmrDevice device;
		private OnDeviceStateChangeListener listener;

		private int volume;
		private PlayState state;
		private PositionInfo info;
		private Handler actionHandler;

		private boolean toContinue = true;

		public SyncHandler(Looper looper, DmrDevice device,
				OnDeviceStateChangeListener listener, Handler actionHandler) {
			super(looper);
			this.device = device;
			this.listener = listener;
			this.actionHandler = actionHandler;
			volume = -1;
			state = PlayState.FALSE;
			info = new PositionInfo();
			info.duration = 0;
			info.position = 0;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (ActionManager.getInstance()
						.getVolume(device, actionHandler) != volume) {
					volume = ActionManager.getInstance().getVolume(device,
							actionHandler);
					listener.onVolumeChanged(volume);
				}
				if (toContinue)
					this.sendEmptyMessageDelayed(1, 1000);
				break;
			case 2:
				if (info == null
						|| ActionManager.getInstance().getPositionInfo1(device,
								actionHandler) == null) {
					return;
				}
				if (ActionManager.getInstance().getPositionInfo1(device,
						actionHandler).position != info.position) {
					info = ActionManager.getInstance().getPositionInfo1(device,
							actionHandler);
					listener.onPositionChanged(info.position, info.duration);
				}
				if (toContinue)
					this.sendEmptyMessageDelayed(2, 1000);
				break;
			case 3:
				if (ActionManager.getInstance().getCurrentAction(device,
						actionHandler) != state) {
					state = ActionManager.getInstance().getCurrentAction(
							device, actionHandler);
					listener.onPlayStateChanged(state);
				}
				if (toContinue)
					this.sendEmptyMessageDelayed(3, 1000);
				break;
			default:
				break;
			}
		}

		public void start() {
			this.sendEmptyMessage(1);
			this.sendEmptyMessage(2);
			this.sendEmptyMessage(3);
		}

		public void stop() {
			toContinue = false;
		}
	}

	private boolean checkActionIsPrepared() {
		if (WifiReceiver.getInstance().isConnected()
				&& ServiceManager.getInstance().isDmcOpen()) {
			if (Thread.currentThread().getState() == State.WAITING) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	public MediaInfo getPushingMedia() {
		return pushingMedia;
	}

	private void blockCurrentThread() {
		synchronized (Thread.currentThread()) {
			try {
				Thread.currentThread().wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private String parseMillisecond(long millisecond) {
		StringBuffer sb = new StringBuffer();
		int hour = (int) (millisecond / 3600000);
		if (hour >= 10) {
			sb.append(hour);
		} else {
			sb.append("0" + hour);
		}
		sb.append(":");
		int min = (int) ((millisecond - hour * 3600000) / 60000);
		if (min >= 10) {
			sb.append(min);
		} else {
			sb.append("0" + min);
		}
		sb.append(":");
		int second = (int) ((millisecond - hour * 3600000 - min * 60000) / 1000);
		if (second >= 10) {
			sb.append(second);
		} else {
			sb.append("0" + second);
		}
		sb.append(".");
		int milli = (int) (millisecond % 1000);
		if (milli >= 100) {
			sb.append(milli);
		} else if (milli >= 10) {
			sb.append("0" + milli);
		} else {
			sb.append("00" + milli);
		}
		log.warning("parse million secound =" + sb.toString());
		return sb.toString();
	}

	public void releaseCurrentThread(Thread thread) {
		synchronized (thread) {
			thread.notify();
		}
	}

}
