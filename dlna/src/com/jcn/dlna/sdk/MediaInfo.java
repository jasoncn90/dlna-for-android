package com.jcn.dlna.sdk;

import java.io.File;

import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.Movie;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.model.item.Photo;
import org.teleal.common.util.MimeType;

import com.jcn.dlna.sdk.dms.httpserver.HttpServerManager;

/**
 * 多媒体资源信息
 */
public class MediaInfo {

	/**
	 * 多媒体文件类型
	 */
	public static enum MediaMIMEType {
		VIDEO, AUDIO, IMAGE
	}

	public MediaInfo() {
	}

	/**
	 * 用于网络视频
	 * 
	 * @param url
	 *            starts with "http:"
	 * @param title
	 */
	public MediaInfo(String url, String title) {
		this.setTitle(title);
		this.setUrl(url);
	}

	/**
	 * 用于本地视频
	 * 
	 * @param url
	 *            文件路径，如"mnt/sdcard/video/xxx.mp4"
	 * @param title
	 *            多媒体标题
	 * @param size
	 *            多媒体文件大小
	 * @param mimeType
	 *            由cursor获得的mimeType
	 */
	public MediaInfo(String url, String title, long size, String mimeType) {
		this.setUrl(url);
		this.setTitle(title);
		this.setSize(size);
		this.setMimeType(mimeType);
	}

	/**
	 * 用于本地视频
	 * 
	 * @param url
	 *            文件路径，如"mnt/sdcard/video/xxx.mp4"
	 * @param title
	 *            多媒体标题
	 * @param size
	 *            多媒体文件大小
	 * @param mimeType
	 *            {@link MediaMIMEType}
	 */
	public MediaInfo(String url, String title, long size, MediaMIMEType mimeType) {
		this.setUrl(url);
		this.setTitle(title);
		this.setSize(size);
		this.setMimeType(mimeType);
	}

	private String url;

	private String title;

	private long size;

	private MimeType mimeType;

	/**
	 * 获取推送到dlna设备的实际url，若为网络视频，则返回网络url，若本地视频，则返回虚拟的url
	 * 
	 * @return url
	 */
	public String getPushUrl() {
		if (url.startsWith("http")) {
			return url;
		} else {
			return HttpServerManager.getInstance().getVirtualUrl()
					+ System.currentTimeMillis();
		}
	}

	public String getMetaData() {
		Item media = null;
		if (mimeType != null) {
			if (mimeType.getType().equals("video")) {
				media = new Movie();
			} else if (mimeType.getType().equals("audio")) {
				media = new MusicTrack();
			} else if (mimeType.getType().equals("image")) {
				media = new Photo();
			}
		} else {
			media = new Movie();
		}
		if (title != null) {
			media.setTitle(title);
		}
		Res resource = new Res() {
			@Override
			public String getValue() {
				return getPushUrl();
			}
		};
		resource.setProtocolInfo(new ProtocolInfo(
				mimeType == null ? new MimeType() : mimeType));
		if (size != 0) {
			resource.setSize(size);
		} else {
			if (!url.startsWith("http")) {
				File file = new File(url);
				if (file != null && file.isFile()) {
					size = file.length();
					resource.setSize(size);
				}
			}
		}
		media.setId("0");
		media.setParentID("0");
		media.setCreator("System");
		media.addResource(resource);
		DIDLContent didl = new DIDLContent();
		didl.addItem(media);
		String metadata = null;
		try {
			metadata = new DIDLParser().generate(didl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return metadata;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getMimeType() {
		return mimeType.toString();
	}

	public void setMimeType(String mimeType) {
		this.mimeType = MimeType.valueOf(mimeType);
	}

	public void setMimeType(MediaMIMEType mimeType) {
		switch (mimeType) {
		case VIDEO:
			this.mimeType = MimeType.valueOf("video/*");
			break;
		case AUDIO:
			this.mimeType = MimeType.valueOf("audio/*");
			break;
		case IMAGE:
			this.mimeType = MimeType.valueOf("image/*");
			break;
		default:
			break;
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String path) {
		this.url = path;
	}

	@Override
	public String toString() {
		return "MediaInfo [url=" + url + ", title=" + title + ", size=" + size
				+ ", mimeType=" + mimeType + "]";
	}

}
