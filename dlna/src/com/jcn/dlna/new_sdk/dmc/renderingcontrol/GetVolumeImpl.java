package com.jcn.dlna.new_sdk.dmc.renderingcontrol;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.renderingcontrol.callback.GetVolume;

@SuppressWarnings("rawtypes")
public class GetVolumeImpl extends GetVolume {

	private GetVolumeListener listener;

	public interface GetVolumeListener {

		public void onResult(boolean success, int errorCode, String errorMsg,
				int volume);
	}

	public GetVolumeImpl(Service<?, ?> service, GetVolumeListener listener) {
		super(service);
		this.listener = listener;
	}

	@Override
	public void received(ActionInvocation invocation, int arg1) {
		listener.onResult(true, 0, null, arg1);
	}

	@Override
	public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
		listener.onResult(false, arg1 != null ? arg1.getStatusCode() : -1,
				arg2, -1);
	}

}
