package com.jcn.dlna.new_sdk.dmc.renderingcontrol;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.renderingcontrol.callback.GetMute;

@SuppressWarnings("rawtypes")
public class GetMuteImpl extends GetMute {

	private GetMuteListener listener;

	public interface GetMuteListener {

		public void onResult(boolean success, int errorCode, String errorMsg,
				boolean mute);
	}

	public GetMuteImpl(Service<?, ?> service, GetMuteListener listener) {
		super(service);
		this.listener = listener;
	}

	@Override
	public void received(ActionInvocation invocation, boolean arg1) {
		listener.onResult(true, 0, null, arg1);
	}

	@Override
	public void failure(ActionInvocation invocation, UpnpResponse arg1,
			String arg2) {
		listener.onResult(false, arg1 != null ? arg1.getStatusCode() : -1,
				arg2, false);
	}

}
