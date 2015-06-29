package com.jcn.dlna.sdk.dmc.avtransport;

import java.util.logging.Logger;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;

import com.jcn.dlna.sdk.dmc.ActionManager;

@SuppressWarnings("rawtypes")
public class SetAVTransportURIImpl extends SetAVTransportURI {

	private static final Logger log = Logger
			.getLogger(SetAVTransportURIImpl.class.getSimpleName());
	private Thread thread;

	private boolean result;

	public SetAVTransportURIImpl(Service<?, ?> service, String uri,
			String metadata, Thread thread) {
		super(service, uri, metadata);
		this.thread = thread;
	}

	@Override
	public void success(ActionInvocation invocation) {
		super.success(invocation);
		log.info("success-->" + invocation.toString());
		log.warning("input map -->" + invocation.getInputMap().toString());
		result = true;
		ActionManager.getInstance().releaseCurrentThread(thread);
	}

	@Override
	public void failure(ActionInvocation invocation, UpnpResponse arg1,
			String arg2) {
		log.severe("failure-->" + invocation.toString());
		log.warning("input map -->" + invocation.getInputMap().toString());
		result = false;
		ActionManager.getInstance().releaseCurrentThread(thread);

	}

	public boolean getResult() {
		return result;
	}

}
