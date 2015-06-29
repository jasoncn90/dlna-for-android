package com.jcn.dlna.sdk.dms.content.movies;

import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.item.Movie;
import org.teleal.common.util.MimeType;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jcn.dlna.sdk.dms.UrlBuilder;
import com.jcn.dlna.sdk.dms.content.MediaStoreContent;
import com.jcn.dlna.sdk.dms.content.MediaStoreItem;

public class MediaStoreMovie extends Movie implements MediaStoreItem {

	private long mediaStoreId;
	private String path;
	private MimeType mimeType;
	private long size;

	public static final String[] PROJECTION = { MediaStore.Video.Media._ID,
			MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DISPLAY_NAME,
			MediaStore.Video.Media.SIZE, MediaStore.Video.Media.MIME_TYPE,
			MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATA };

	public MediaStoreMovie(Cursor cursor, Uri uri) {
		this.mediaStoreId = cursor.getLong(0);
		setId(String.valueOf(mediaStoreId));
		setParentID(MoviesContainer.ID);
		setCreator(MediaStoreContent.CREATOR);

		if (!cursor.isNull(1))
			setTitle(cursor.getString(1));
		else
			setTitle(cursor.getString(2));

		this.size = cursor.getLong(3);

		this.mimeType = MimeType.valueOf(cursor.getString(4));

		Res resource = new Res() {
			@Override
			public String getValue() {
				return UrlBuilder.createResourceUrl(MediaStoreMovie.this);
			}
		};
		resource.setProtocolInfo(new ProtocolInfo(mimeType));
		resource.setSize(size);
		long duration = cursor.getLong(5);
		resource.setDuration(String.valueOf(duration));
		addResource(resource);
		this.path = cursor.getString(6);
		// Log.e("movie", "title-->" + getTitle());
		// Log.i("movie", "mimeType-->" + mimeType);
		// Log.i("movie", "size-->" + size);
		// Log.i("movie", "mediaStoreUri-->" + mediaStoreUri);
		// Log.i("movie", "mediaStoreId-->" + mediaStoreId);
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public MimeType getMimeType() {
		return mimeType;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public long getMediaStoreId() {
		return mediaStoreId;
	}
}
