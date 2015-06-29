package com.jcn.dlna.sdk.dms.content.musics;

import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.common.util.MimeType;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jcn.dlna.sdk.dms.UrlBuilder;
import com.jcn.dlna.sdk.dms.content.MediaStoreContent;
import com.jcn.dlna.sdk.dms.content.MediaStoreItem;

public class MediaStoreMusic extends MusicTrack implements MediaStoreItem {
	private long mediaStoreId;
	private String path;
	private MimeType mimeType;
	private long size;

	public static final String[] PROJECTION = { MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DISPLAY_NAME,
			MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.MIME_TYPE,
			MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA };

	public MediaStoreMusic(Cursor cursor, Uri uri) {
		this.mediaStoreId = cursor.getLong(0);
		setId(String.valueOf(mediaStoreId));
		setParentID(MusicsContainer.ID);
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
				return UrlBuilder.createResourceUrl(MediaStoreMusic.this);
			}
		};
		resource.setProtocolInfo(new ProtocolInfo(mimeType));
		resource.setSize(size);
		long duration = cursor.getLong(5);
		resource.setDuration(String.valueOf(duration));
		addResource(resource);
		this.path = cursor.getString(6);
		// Log.e("music", "title-->" + getTitle());
		// Log.i("music", "mimeType-->" + mimeType);
		// Log.i("music", "size-->" + size);
		// Log.i("music", "mediaStoreUri-->" + mediaStoreUri);
		// Log.i("music", "mediaStoreId-->" + mediaStoreId);
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
