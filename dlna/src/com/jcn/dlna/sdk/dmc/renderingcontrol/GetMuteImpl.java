package com.jcn.dlna.sdk.dmc.renderingcontrol;

import java.util.logging.Logger;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.renderingcontrol.callback.GetMute;

import com.jcn.dlna.sdk.dmc.ActionManager;

@SuppressWarnings("rawtypes")
public class GetMuteImpl extends GetMute {

	private static final Logger log = Logger.getLogger(GetMuteImpl.class
			.getSimpleName());

	private Thread current;
	private boolean result;

	public GetMuteImpl(Service<?, ?> service, Thread current) {
		super(service);
		this.current = current;
	}

	@Override
	public void received(ActionInvocation invocation, boolean arg1) {
		log.info("success-->" + invocation.toString());
		result = arg1;
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
