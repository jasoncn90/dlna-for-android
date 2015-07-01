package com.jcn.dlna.new_sdk.dmc.avtransport;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.model.PositionInfo;

@SuppressWarnings("rawtypes")
public class GetPositionInfoImpl extends GetPositionInfo {

	private GetPositionInfoListener listener;

	public interface GetPositionInfoListener {
		public void onResult(boolean success, int errorCode, String errorMsg,
				long position, long duration);
	}

	public GetPositionInfoImpl(Service service, GetPositionInfoListener listener) {
		super(service);
		this.listener = listener;
	}

	@Override
	public void received(ActionInvocation invocation, PositionInfo arg1) {
		listener.onResult(true, 0, null, arg1.getTrackElapsedSeconds(),
				arg1.getTrackDurationSeconds());
	}

	@Override
	public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
		listener.onResult(true, arg1 != null ? arg1.getStatusCode() : -1, arg2,
				-1, -1);
	}

}
