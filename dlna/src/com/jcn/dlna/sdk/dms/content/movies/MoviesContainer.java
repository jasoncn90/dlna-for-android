package com.jcn.dlna.sdk.dms.content.movies;

import java.util.Iterator;

import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.database.Cursor;
import android.net.Uri;

import com.jcn.dlna.sdk.Dlna;
import com.jcn.dlna.sdk.dms.content.Content;
import com.jcn.dlna.sdk.dms.content.MediaStoreContent;
import com.jcn.dlna.sdk.dms.content.RootContainer;

public class MoviesContainer extends Container implements Content {

	protected MediaStoreContent content;
	public static final String ID = "movies";

	public MoviesContainer(RootContainer rootContainer) {
		this.content = rootContainer.getContent();
		setId(ID);
		setParentID(rootContainer.getId());
		setTitle("Movies");
		setCreator(MediaStoreContent.CREATOR);
		setClazz(MediaStoreContent.CLASS_CONTAINER);
		setRestricted(true);
		setSearchable(false);
		setWriteStatus(WriteStatus.NOT_WRITABLE);

	}

	@Override
	public void update(Uri uri) {
		Cursor cursor = Dlna.getContext().getContentResolver()
				.query(uri, MediaStoreMovie.PROJECTION, null, null, null);
		if (cursor == null || !cursor.moveToFirst())
			return;
		int childCount = 0;
		do {
			boolean exist = false;
			Long id = cursor.getLong(0);
			Iterator<Item> it = this.getItems().iterator();
			while (it.hasNext()) {
				if (it.next().getId().endsWith(String.valueOf(id)))
					exist = true;
			}
			if (exist)
				continue;
			childCount++;
			MediaStoreMovie movie = new MediaStoreMovie(cursor, uri);
			this.addItem(movie);
		} while (cursor.moveToNext());
		cursor.close();
		this.setChildCount(childCount);
	}
}
