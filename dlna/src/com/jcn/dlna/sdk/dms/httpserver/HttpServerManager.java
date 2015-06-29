package com.jcn.dlna.sdk.dms.httpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.teleal.cling.support.model.item.Item;
import org.teleal.common.util.MimeType;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jcn.dlna.sdk.MediaInfo;
import com.jcn.dlna.sdk.dmc.ActionManager;
import com.jcn.dlna.sdk.dms.UrlBuilder;
import com.jcn.dlna.sdk.dms.content.MediaStoreContent;
import com.jcn.dlna.sdk.dms.content.MediaStoreItem;
import com.jcn.dlna.sdk.dms.content.OtherContainer.CustomItem;

public class HttpServerManager {

	private static final Logger log = Logger.getLogger(HttpServerManager.class
			.getSimpleName());

	private HttpServerManager() {
	}

	private static HttpServerManager instance;
	private HttpServerService httpServerService;

	public static HttpServerManager getInstance() {
		if (instance == null) {
			instance = new HttpServerManager();
		}
		return instance;
	}

	private ServiceConnection httpServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (httpServerService != null) {
				httpServerService.removeHandler("*");
			}
			httpServerService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service instanceof HttpServerService) {
				httpServerService = (HttpServerService) service;
				httpServerService.addHandler("*", new ContentHandler());
			}
		}
	};

	public void startHttpServer(Context context) {
		context.bindService(new Intent(context, HttpServerServiceImpl.class),
				httpServiceConnection, Context.BIND_AUTO_CREATE);
	}

	public void stopHttpServer(Context context) {
		try {
			context.unbindService(httpServiceConnection);
		} catch (Exception e) {
		}
	}

	private class ContentHandler implements HttpRequestHandler {

		@SuppressLint("UseValueOf")
		@Override
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext arg2) throws HttpException, IOException {
			MediaInfo media = null;

			String url = request.getRequestLine().getUri();
			log.warning("handle url in httpServiceManager--> " + url);
			InputStream is;
			if (UrlBuilder.isCustomItemUrl(url)) {
				String id = UrlBuilder.getFilePathFromUrl(url);
				CustomItem item = (CustomItem) MediaStoreContent.getInstance()
						.findObjectWithId(id);
				File file = new File(item.getPath());
				log.severe("file id-->" + id);
				log.severe("file path-->" + item.getPath());
				long size = file.length();
				is = new FileInputStream(file);
				InputStreamEntity entity = new InputStreamEntity(is, size);
				response.setEntity(entity);
				response.setStatusCode(HttpStatus.SC_OK);
				return;
			} else if (UrlBuilder.isVirtualUrl(url)) {
				media = getPushMediaInfo();
				log.warning("media info ->" + media.toString());
				is = openDataInputStream(media);
			} else {
				String id = UrlBuilder.parseUrlToMediaStoreId(url);
				MediaStoreItem item = (MediaStoreItem) MediaStoreContent
						.getInstance().findObjectWithId(id);
				if (item == null) {
					log.severe("item not found,return");
					return;
				}
				Item contentItem = (Item) item;
				media = new MediaInfo(item.getPath(), contentItem.getTitle(),
						item.getSize(), item.getMimeType().toString());
				is = openDataInputStream(media);
			}

			if (is == null) {
				log.severe("Data not readable, returning 404");
				response.setStatusCode(HttpStatus.SC_NOT_FOUND);
				return;
			}
			long sizeInBytes = media.getSize();
			MimeType mimeType = MimeType.valueOf(media.getMimeType());

			boolean isKeepAlive = false; // 判断Connection类型
			Header[] requestHeaders = request.getAllHeaders();
			for (Header header : requestHeaders) {
				if (header.getName().equals("Connection")
						&& header.getValue().equals("Keep-Alive")) {
					isKeepAlive = true;
				}
			}
			if (isKeepAlive) {
				InputStreamEntity entity = new InputStreamEntity(is,
						sizeInBytes);
				entity.setContentType(mimeType.toString());
				response.setEntity(entity);
				response.setStatusCode(HttpStatus.SC_OK);
			} else {
				long contentLength = 0;// 客户端请求的字节总量
				long pastLength = 0;// 记录已下载文件大小
				String rangeBytes = "";// 记录客户端传来的形如“bytes=27000-”的内容
				String range = null;
				Header[] headers = request.getAllHeaders();
				for (Header reheader : headers) {
					if (reheader.getName().equals("Range")) {
						range = reheader.getValue();
					}
				}
				if (range != null) {
					rangeBytes = range.replaceAll("bytes=", "");
					if (rangeBytes.endsWith("-")) {
						rangeBytes = rangeBytes.substring(0,
								rangeBytes.indexOf('-'));
						pastLength = Long.parseLong(rangeBytes.trim());
						contentLength = sizeInBytes - pastLength;
						String contentRange = new StringBuffer("bytes ")
								.append(new Long(pastLength).toString())
								.append("-")
								.append(Long.valueOf(sizeInBytes - 1))
								.append("/").append(Long.valueOf(sizeInBytes))
								.toString();
						response.setHeader("Content-Range", contentRange);
						is.skip(pastLength);
					}
				} else {
					contentLength = sizeInBytes;
				}
				InputStreamEntity entity = new InputStreamEntity(is,
						contentLength);
				entity.setContentType(mimeType.toString());
				response.setEntity(entity);
				if (pastLength != sizeInBytes) {
					response.setStatusCode(HttpStatus.SC_PARTIAL_CONTENT);
				} else {
					response.setStatusCode(HttpStatus.SC_OK);
				}
			}
		}

		private InputStream openDataInputStream(MediaInfo media) {
			try {
				return new FileInputStream(new File(media.getUrl()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private MediaInfo getPushMediaInfo() {
		return ActionManager.getInstance().getPushingMedia();
	}

	/**
	 * 随意创建一个url，让dmr发送请求到本地的http
	 * server上就行，而推送的视频因为一个操作只有一个，就不用通过contentdirectory去寻找了，直接从ActionManager中拿出来
	 * 
	 * @return url
	 */
	public String getVirtualUrl() {
		return UrlBuilder.createVirtualUrl();
	}
}
