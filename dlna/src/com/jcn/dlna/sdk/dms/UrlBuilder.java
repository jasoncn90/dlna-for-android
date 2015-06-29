package com.jcn.dlna.sdk.dms;

import java.net.URI;

import org.teleal.common.util.URIUtil;

import com.jcn.dlna.sdk.dms.content.OtherContainer.CustomItem;
import com.jcn.dlna.sdk.dms.content.movies.MediaStoreMovie;
import com.jcn.dlna.sdk.dms.content.movies.MoviesContainer;
import com.jcn.dlna.sdk.dms.content.musics.MediaStoreMusic;
import com.jcn.dlna.sdk.dms.content.musics.MusicsContainer;
import com.jcn.dlna.sdk.dms.content.photos.MediaStorePhoto;
import com.jcn.dlna.sdk.dms.content.photos.PhotosContainer;
import com.jcn.dlna.sdk.dms.httpserver.HttpServer;

/**
 * 负责创建虚拟的url，以及相关的判断
 */
public class UrlBuilder {

	public static String createResourceUrl(MediaStoreMovie movie) {
		return URIUtil.createAbsoluteURL(
				HttpServer.getInstance().getLocalInetAddress(),
				HttpServer.getInstance().getLocalPort(),
				URI.create("/" + MoviesContainer.ID + "/"
						+ movie.getMediaStoreId())).toString();
	}

	public static String createResourceUrl(MediaStorePhoto photo) {
		return URIUtil.createAbsoluteURL(
				HttpServer.getInstance().getLocalInetAddress(),
				HttpServer.getInstance().getLocalPort(),
				URI.create("/" + PhotosContainer.ID + "/"
						+ photo.getMediaStoreId())).toString();
	}

	public static String createResourceUrl(MediaStoreMusic music) {
		return URIUtil.createAbsoluteURL(
				HttpServer.getInstance().getLocalInetAddress(),
				HttpServer.getInstance().getLocalPort(),
				URI.create("/" + MusicsContainer.ID + "/"
						+ music.getMediaStoreId())).toString();
	}

	public static boolean isCustomItemUrl(String url) {
		if (url.contains("file")) {
			return true;
		} else {
			return false;
		}
	}

	public static String createUrlByCustomItem(CustomItem item) {
		String url = URIUtil.createAbsoluteURL(
				HttpServer.getInstance().getLocalInetAddress(),
				HttpServer.getInstance().getLocalPort(),
				URI.create("/file/" + item.getId())).toString();
		return url;
	}

	public static String getFilePathFromUrl(String url) {
		int index = url.indexOf("file");
		return url.substring(index + 5);
	}

	public static String parseUrlToMediaStoreId(String url) {
		int index = url.lastIndexOf("/") + 1;
		return url.substring(index);
	}

	public static String createVirtualUrl() {
		return URIUtil.createAbsoluteURL(
				HttpServer.getInstance().getLocalInetAddress(),
				HttpServer.getInstance().getLocalPort(),
				URI.create("/virtual_url")).toString();
	}

	public static boolean isVirtualUrl(String url) {
		if (url.contains("virtual_url")) {
			return true;
		} else {
			return false;
		}
	}
}
