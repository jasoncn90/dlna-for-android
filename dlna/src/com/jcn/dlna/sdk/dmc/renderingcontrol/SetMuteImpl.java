package com.jcn.dlna.sdk.dmc.renderingcontrol;

import java.util.logging.Logger;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.renderingcontrol.callback.SetMute;

import com.jcn.dlna.sdk.dmc.ActionManager;

@SuppressWarnings("rawtypes")
public class SetMuteImpl extends SetMute {

	private static final Logger log = Logger.getLogger(SetMuteImpl.class
			.getSimpleName());

	private Thread current;
	private boolean result;

	public SetMuteImpl(Service<?, ?> service, boolean desiredMute,
			Thread current) {
		super(service, desiredMute);
		this.current = current;
	}

	@Override
	public void success(ActionInvocation invocation) {
		super.success(invocation);
		log.info("success-->" + invocation.toString());
		result = true;
		ActionManager.getInstance().releaseCurrentThread(current);
	}

	@Override
	public void failure(ActionInvocation invocation, UpnpResponse arg1,
			String arg2) {
		log.severe("failure-->" + invocation.toString());
		result = false;
		ActionManager.getInstance().releaseCurrentThread(current);
	}

	public boolean getResult() {
		return result;
	}
}
