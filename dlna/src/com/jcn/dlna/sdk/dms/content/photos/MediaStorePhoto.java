package com.jcn.dlna.sdk.dms.content.photos;

import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.item.Photo;
import org.teleal.common.util.MimeType;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jcn.dlna.sdk.dms.UrlBuilder;
import com.jcn.dlna.sdk.dms.content.MediaStoreContent;
import com.jcn.dlna.sdk.dms.content.MediaStoreItem;

public class MediaStorePhoto extends Photo implements MediaStoreItem {
	private long mediaStoreId;
	private String path;
	private MimeType mimeType;
	private long size;

	public static final String[] PROJECTION = { MediaStore.Images.Media._ID,
			MediaStore.Images.Media.TITLE,
			MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE,
			MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.DATA };

	public MediaStorePhoto(Cursor cursor, Uri uri) {
		this.mediaStoreId = cursor.getLong(0);
		setId(String.valueOf(mediaStoreId));
		setParentID(PhotosContainer.ID);
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
				return UrlBuilder.createResourceUrl(MediaStorePhoto.this);
			}
		};
		resource.setProtocolInfo(new ProtocolInfo(mimeType));
		resource.setSize(size);
		addResource(resource);
		this.path = cursor.getString(5);
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
