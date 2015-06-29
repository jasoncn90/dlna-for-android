package com.jcn.dlna.sdk.dmc.avtransport;

import java.util.logging.Logger;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.avtransport.callback.GetTransportInfo;
import org.teleal.cling.support.model.TransportInfo;

import com.jcn.dlna.sdk.dmc.ActionManager;
import com.jcn.dlna.sdk.dmc.DmrDevice.PlayState;

@SuppressWarnings("rawtypes")
public class GetTransportInfoImpl extends GetTransportInfo {

	private static final Logger log = Logger
			.getLogger(GetTransportInfoImpl.class.getSimpleName());

	private Thread current;
	private PlayState result;

	public GetTransportInfoImpl(Service<?, ?> service, Thread current) {
		super(service);
		this.current = current;
	}

	@Override
	public void received(ActionInvocation invocation, TransportInfo arg1) {
		log.info("success-->" + invocation.toString());
		log.warning("transport info state=" + arg1.getCurrentTransportState());
		PlayState result = null;
		switch (arg1.getCurrentTransportState()) {
		case CUSTOM:
		case NO_MEDIA_PRESENT:
		case RECORDING:
		case STOPPED:
			result = PlayState.STOPPED;
			break;
		case PAUSED_PLAYBACK:
		case PAUSED_RECORDING:
			result = PlayState.PAUSED;
			break;
		case PLAYING:
			result = PlayState.PLAYING;
			break;
		case TRANSITIONING:
			result = PlayState.LOADING;
			break;
		default:
			result = PlayState.FALSE;
			break;
		}
		this.result = result;
		ActionManager.getInstance().releaseCurrentThread(current);
	}

	@Override
	public void failure(ActionInvocation invocation, UpnpResponse arg1,
			String arg2) {
		log.severe("false-->" + invocation.toString());
		this.result = PlayState.FALSE;
		ActionManager.getInstance().releaseCurrentThread(current);
	}

	public PlayState getResult() {
		return result;
	}
}
