package com.jcn.dlna.new_sdk;

public class DlnaUtils {

	public static String parseMillisecond(long millisecond) {
		StringBuffer sb = new StringBuffer();
		int hour = (int) (millisecond / 3600000);
		if (hour >= 10) {
			sb.append(hour);
		} else {
			sb.append("0" + hour);
		}
		sb.append(":");
		int min = (int) ((millisecond - hour * 3600000) / 60000);
		if (min >= 10) {
			sb.append(min);
		} else {
			sb.append("0" + min);
		}
		sb.append(":");
		int second = (int) ((millisecond - hour * 3600000 - min * 60000) / 1000);
		if (second >= 10) {
			sb.append(second);
		} else {
			sb.append("0" + second);
		}
		sb.append(".");
		int milli = (int) (millisecond % 1000);
		if (milli >= 100) {
			sb.append(milli);
		} else if (milli >= 10) {
			sb.append("0" + milli);
		} else {
			sb.append("00" + milli);
		}
		return sb.toString();
	}
}
