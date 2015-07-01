package com.jcn.dlna.new_sdk.dmc;

import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UDAServiceType;

import com.jcn.dlna.new_sdk.Constants;
import com.jcn.dlna.new_sdk.DlnaService;
import com.jcn.dlna.new_sdk.device.DmrDevice;
import com.jcn.dlna.new_sdk.dmc.renderingcontrol.SetMuteImpl;

public class ActionController {

	private static final UDAServiceType AV_TRANSPORT_TYPE = new UDAServiceType(
			"AVTransport");
	private static final UDAServiceType RENDERING_CONTROL_TYPE = new UDAServiceType(
			"RenderingControl");

	public void setMute(DmrDevice device, boolean desiredMute,
			ActionResultListener listener) {
		Service<?, ?> dlnaService = device.findService(RENDERING_CONTROL_TYPE);
		if (dlnaService == null) {
			listener.onResult(false, Constants.ERRCODE_ACTION_UNSUPPORT,
					Constants.ERRMSG_ACTION_UNSUPPORT);
		}
		final SetMuteImpl setMute = new SetMuteImpl(dlnaService, desiredMute,
				listener);
		new Thread(new Runnable() {

			@Override
			public void run() {
				DlnaService.getInstance().getService().getControlPoint()
						.execute(setMute);
			}
		}).start();
	}

	public interface ActionResultListener {
		void onResult(boolean success, int errorCode, String errorMsg);
	}
}
