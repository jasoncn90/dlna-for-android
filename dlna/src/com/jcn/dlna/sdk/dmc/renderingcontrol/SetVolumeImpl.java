package com.jcn.dlna.sdk.dmc.renderingcontrol;

import java.util.logging.Logger;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.renderingcontrol.callback.SetVolume;

import com.jcn.dlna.sdk.dmc.ActionManager;

@SuppressWarnings("rawtypes")
public class SetVolumeImpl extends SetVolume {

	private static final Logger log = Logger.getLogger(SetVolumeImpl.class
			.getSimpleName());

	private Thread current;
	private boolean result;

	public SetVolumeImpl(Service<?, ?> service, long newVolume, Thread current) {
		super(service, newVolume);
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
		log.info("success-->" + invocation.toString());
		result = false;
		ActionManager.getInstance().releaseCurrentThread(current);
	}

	public boolean getResult() {
		return result;
	}
}
