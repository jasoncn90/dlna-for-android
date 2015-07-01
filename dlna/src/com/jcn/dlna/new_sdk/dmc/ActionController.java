package com.jcn.dlna.new_sdk.dmc;

import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UDAServiceType;

import com.jcn.dlna.new_sdk.Constants;
import com.jcn.dlna.new_sdk.DlnaService;
import com.jcn.dlna.new_sdk.DlnaUtils;
import com.jcn.dlna.new_sdk.device.DmrDevice;
import com.jcn.dlna.new_sdk.dmc.avtransport.GetPositionInfoImpl;
import com.jcn.dlna.new_sdk.dmc.avtransport.GetPositionInfoImpl.GetPositionInfoListener;
import com.jcn.dlna.new_sdk.dmc.avtransport.GetTransportInfoImpl;
import com.jcn.dlna.new_sdk.dmc.avtransport.GetTransportInfoImpl.GetTransportStateListener;
import com.jcn.dlna.new_sdk.dmc.avtransport.PauseImpl;
import com.jcn.dlna.new_sdk.dmc.avtransport.PlayImpl;
import com.jcn.dlna.new_sdk.dmc.avtransport.SeekImpl;
import com.jcn.dlna.new_sdk.dmc.avtransport.StopImpl;
import com.jcn.dlna.new_sdk.dmc.renderingcontrol.GetMuteImpl;
import com.jcn.dlna.new_sdk.dmc.renderingcontrol.GetMuteImpl.GetMuteListener;
import com.jcn.dlna.new_sdk.dmc.renderingcontrol.GetVolumeImpl;
import com.jcn.dlna.new_sdk.dmc.renderingcontrol.GetVolumeImpl.GetVolumeListener;
import com.jcn.dlna.new_sdk.dmc.renderingcontrol.SetMuteImpl;
import com.jcn.dlna.new_sdk.dmc.renderingcontrol.SetVolumeImpl;

public class ActionController {

	private static final UDAServiceType AV_TRANSPORT_TYPE = new UDAServiceType(
			"AVTransport");
	private static final UDAServiceType RENDERING_CONTROL_TYPE = new UDAServiceType(
			"RenderingControl");

	public void getTransportStatus(DmrDevice device,
			GetTransportStateListener listener) {
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			listener.onResult(false, Constants.ERRCODE_ACTION_UNSUPPORT,
					Constants.ERRMSG_ACTION_UNSUPPORT, null);
			return;
		}
		final GetTransportInfoImpl getTransportStatus = new GetTransportInfoImpl(
				dlnaService, listener);
		execute(getTransportStatus);
	}

	public void getPostiion(DmrDevice device, GetPositionInfoListener listener) {
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			listener.onResult(false, Constants.ERRCODE_ACTION_UNSUPPORT,
					Constants.ERRMSG_ACTION_UNSUPPORT, -1, -1);
			return;
		}
		final GetPositionInfoImpl getPosition = new GetPositionInfoImpl(
				dlnaService, listener);
		execute(getPosition);
	}

	public void stop(DmrDevice device, ActionResultListener listener) {
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			serviceNotFoundCallback(listener);
			return;
		}
		final StopImpl stop = new StopImpl(dlnaService, listener);
		execute(stop);
	}

	public void pause(DmrDevice device, ActionResultListener listener) {
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			serviceNotFoundCallback(listener);
			return;
		}
		final PauseImpl pause = new PauseImpl(dlnaService, listener);
		execute(pause);
	}

	public void resume(DmrDevice device, ActionResultListener listener) {
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			serviceNotFoundCallback(listener);
			return;
		}
		final PlayImpl play = new PlayImpl(dlnaService, listener);
		execute(play);
	}

	public void seek(DmrDevice device, long millisecond,
			ActionResultListener listener) {
		Service<?, ?> dlnaService = device.findService(AV_TRANSPORT_TYPE);
		if (dlnaService == null) {
			serviceNotFoundCallback(listener);
			return;
		}
		final SeekImpl seek = new SeekImpl(dlnaService,
				DlnaUtils.parseMillisecond(millisecond), listener);
		execute(seek);
	}

	public void setMute(DmrDevice device, boolean desiredMute,
			ActionResultListener listener) {
		Service<?, ?> dlnaService = device.findService(RENDERING_CONTROL_TYPE);
		if (dlnaService == null) {
			serviceNotFoundCallback(listener);
			return;
		}
		SetMuteImpl setMute = new SetMuteImpl(dlnaService, desiredMute,
				listener);
		execute(setMute);
	}

	public void getMute(DmrDevice device, GetMuteListener listener) {
		Service<?, ?> dlnaService = device.findService(RENDERING_CONTROL_TYPE);
		if (dlnaService == null) {
			listener.onResult(false, Constants.ERRCODE_ACTION_UNSUPPORT,
					Constants.ERRMSG_ACTION_UNSUPPORT, false);
			return;
		}
		final GetMuteImpl getMute = new GetMuteImpl(dlnaService, listener);
		execute(getMute);
	}

	public void setVolume(DmrDevice device, long volume,
			ActionResultListener listener) {
		Service<?, ?> dlnaService = device.findService(RENDERING_CONTROL_TYPE);
		if (dlnaService == null) {
			serviceNotFoundCallback(listener);
			return;
		}
		final SetVolumeImpl setVolume = new SetVolumeImpl(dlnaService, volume,
				listener);
		execute(setVolume);
	}

	public void getVolume(DmrDevice device, GetVolumeListener listener) {
		Service<?, ?> dlnaService = device.findService(RENDERING_CONTROL_TYPE);
		if (dlnaService == null) {
			listener.onResult(false, Constants.ERRCODE_ACTION_UNSUPPORT,
					Constants.ERRMSG_ACTION_UNSUPPORT, -1);
			return;
		}
		final GetVolumeImpl getVolume = new GetVolumeImpl(dlnaService, listener);
		execute(getVolume);
	}

	private void serviceNotFoundCallback(ActionResultListener listener) {
		listener.onResult(false, Constants.ERRCODE_ACTION_UNSUPPORT,
				Constants.ERRMSG_ACTION_UNSUPPORT);
	}

	private void execute(ActionCallback action) {
		DlnaService.getInstance().getService().getControlPoint()
				.execute(action);
	}

	public interface ActionResultListener {

		public void onResult(boolean success, int errorCode, String errorMsg);

	}
}
