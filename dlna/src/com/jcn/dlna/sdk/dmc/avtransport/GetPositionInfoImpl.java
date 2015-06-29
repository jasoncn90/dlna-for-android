package com.jcn.dlna.sdk.dmc.avtransport;

import java.util.logging.Logger;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.model.PositionInfo;

import com.jcn.dlna.sdk.dmc.ActionManager;

@SuppressWarnings("rawtypes")
public class GetPositionInfoImpl extends GetPositionInfo {

	private static final Logger log = Logger
			.getLogger(GetPositionInfoImpl.class.getSimpleName());

	private Thread current;
	private long position;
	private long duration;

	public GetPositionInfoImpl(Service service, Thread current) {
		super(service);
		this.current = current;
	}

	@Override
	public void received(ActionInvocation invocation, PositionInfo arg1) {
		log.info("success-->" + invocation.toString());
		position = arg1.getTrackElapsedSeconds();
		duration = arg1.getTrackDurationSeconds();
		ActionManager.getInstance().releaseCurrentThread(current);
	}

	@Override
	public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
		log.severe("failure-->" + arg0.toString());
		position = Long.valueOf(-1);
		ActionManager.getInstance().releaseCurrentThread(current);
	}

	public long getPosition() {
		return position;
	}

	public long getDuration() {
		return duration;
	}

}
