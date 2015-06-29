package com.jcn.dlna.sdk.dmc.avtransport;

import java.util.logging.Logger;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.avtransport.callback.Seek;

import com.jcn.dlna.sdk.dmc.ActionManager;

@SuppressWarnings("rawtypes")
public class SeekImpl extends Seek {

	private static final Logger log = Logger.getLogger(SeekImpl.class
			.getSimpleName());

	private Thread current;
	private boolean result;

	public SeekImpl(Service service, String relativeTimeTarget, Thread current) {
		super(service, relativeTimeTarget);
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
	public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
		log.severe("failure-->" + arg0.toString());
		log.severe("failure-->" + arg0.getInputMap().toString());

		result = false;
		ActionManager.getInstance().releaseCurrentThread(current);
	}

	public boolean getResult() {
		return result;
	}
}
