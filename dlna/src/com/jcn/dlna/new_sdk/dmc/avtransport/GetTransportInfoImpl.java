package com.jcn.dlna.new_sdk.dmc.avtransport;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.avtransport.callback.GetTransportInfo;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.model.TransportState;

@SuppressWarnings("rawtypes")
public class GetTransportInfoImpl extends GetTransportInfo {

	private GetTransportStateListener listener;

	public interface GetTransportStateListener {
		public void onResult(boolean success, int errorCode, String errorMsg,
				TransportState state);
	}

	public GetTransportInfoImpl(Service<?, ?> service,
			GetTransportStateListener listener) {
		super(service);
		this.listener = listener;
	}

	@Override
	public void received(ActionInvocation invocation, TransportInfo arg1) {
		listener.onResult(true, 0, null, arg1.getCurrentTransportState());
	}

	@Override
	public void failure(ActionInvocation invocation, UpnpResponse arg1,
			String arg2) {
		listener.onResult(false, arg1 != null ? arg1.getStatusCode() : -1,
				arg2, null);
	}

}
