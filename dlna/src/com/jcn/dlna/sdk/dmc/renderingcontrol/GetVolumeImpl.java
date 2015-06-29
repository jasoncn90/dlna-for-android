package com.jcn.dlna.sdk.dmc.renderingcontrol;

import java.util.logging.Logger;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.renderingcontrol.callback.GetVolume;

import com.jcn.dlna.sdk.dmc.ActionManager;

@SuppressWarnings("rawtypes")
public class GetVolumeImpl extends GetVolume {

	private static final Logger log = Logger.getLogger(GetVolumeImpl.class
			.getSimpleName());

	private Thread current;
	private int result;

	public GetVolumeImpl(Service<?, ?> service, Thread current) {
		super(service);
		this.current = current;
	}

	@Override
	public void received(ActionInvocation invocation, int arg1) {
		log.info("success-->" + invocation.toString());
		result = arg1;
		ActionManager.getInstance().releaseCurrentThread(current);
	}

	@Override
	public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
		log.severe("failure-->" + arg0.toString());
		result = -1;
		ActionManager.getInstance().releaseCurrentThread(current);
	}

	public int getResult() {
		return result;
	}
}
