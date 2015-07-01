package com.jcn.dlna.new_sdk.dmc.renderingcontrol;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.renderingcontrol.callback.SetVolume;

import com.jcn.dlna.new_sdk.dmc.ActionController.ActionResultListener;

@SuppressWarnings("rawtypes")
public class SetVolumeImpl extends SetVolume {

	private ActionResultListener listener;

	public SetVolumeImpl(Service<?, ?> service, long newVolume,
			ActionResultListener listener) {
		super(service, newVolume);
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
