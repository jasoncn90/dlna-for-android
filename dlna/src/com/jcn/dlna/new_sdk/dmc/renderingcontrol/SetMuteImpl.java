package com.jcn.dlna.new_sdk.dmc.renderingcontrol;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.renderingcontrol.callback.SetMute;

import com.jcn.dlna.new_sdk.dmc.ActionController.ActionResultListener;

@SuppressWarnings("rawtypes")
public class SetMuteImpl extends SetMute {

	private ActionResultListener listener;

	public SetMuteImpl(Service<?, ?> service, boolean desiredMute,
			ActionResultListener listener) {
		super(service, desiredMute);
		this.listener = listener;
	}

	@Override
	public void success(ActionInvocation invocation) {
		super.success(invocation);
		listener.onResult(true, 0, null);
	}

	@Override
	public void failure(ActionInvocation invocation, UpnpResponse arg1,
			String arg2) {
		listener.onResult(false, arg1 != null ? arg1.getStatusCode() : -1, arg2);
	}

}
