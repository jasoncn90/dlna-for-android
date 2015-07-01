package com.jcn.dlna.new_sdk.dmc.avtransport;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.avtransport.callback.Play;

import com.jcn.dlna.new_sdk.dmc.ActionController.ActionResultListener;

@SuppressWarnings("rawtypes")
public class PlayImpl extends Play {

	private ActionResultListener listener;

	public PlayImpl(Service<?, ?> service, ActionResultListener listener) {
		super(service);
		this.listener = listener;
	}

	@Override
	public void success(ActionInvocation invocation) {
		super.success(invocation);
		listener.onResult(true, 0, null);
	}

	@Override
	public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
		listener.onResult(false, arg1 != null ? arg1.getStatusCode() : -1, arg2);
	}

}
