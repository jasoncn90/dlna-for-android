/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jcn.dlna.sdk.dms.content;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;

import android.net.Uri;

import com.jcn.dlna.sdk.dms.content.OtherContainer.CustomContainer;
import com.jcn.dlna.sdk.dms.content.OtherContainer.CustomItem;
import com.jcn.dlna.sdk.dms.content.movies.MoviesContainer;
import com.jcn.dlna.sdk.dms.content.musics.MusicsContainer;
import com.jcn.dlna.sdk.dms.content.photos.PhotosContainer;

public class RootContainer extends Container {

	final protected MediaStoreContent content;

	private int childCount;
	private boolean photosAdded = false;
	private boolean musicsAdded = false;
	private boolean moviesAdded = false;
	private OtherContainer otherContainer;

	public RootContainer(MediaStoreContent content) {
		this.content = content;
		setId("0");
		setParentID("-1");
		setTitle("Root");
		setCreator(MediaStoreContent.CREATOR);
		setClazz(MediaStoreContent.CLASS_CONTAINER);
		setRestricted(true);
		setSearchable(false);
		setWriteStatus(WriteStatus.NOT_WRITABLE);

		setChildCount(0);
	}

	public MediaStoreContent getContent() {
		return content;
	}

	public boolean addOtherContainerByPath(String path) {
		File file = new File(path);
		DIDLObject object = content.findObjectWithId(createId(path));
		if (object != null)
			return false;
		if (file.isFile()) {
			if (otherContainer == null) {
				otherContainer = new OtherContainer(this);
				addContainer(otherContainer);
			}
			otherContainer.addItem(createItem(file));
			otherContainer.setChildCount(1);
			return true;
		} else if (file.isDirectory()) {
			if (otherContainer == null) {
				otherContainer = new OtherContainer(this);
				addContainer(otherContainer);
			}
			CustomContainer firstContainer = new CustomContainer(
					otherContainer, file.getAbsolutePath(), file.getName());
			otherContainer.addContainer(firstContainer);
			otherContainer.setChildCount(1);
			File[] files = file.listFiles();
			if (files == null || files.length < 1)
				return true;
			addFiles(files, firstContainer);
			return true;
		}
		return false;
	}

	private void addFiles(final File[] files, final CustomContainer parent) {
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				parent.addItem(createItem(files[i]));
				parent.setChildCount(i);
			} else if (files[i].isDirectory()) {
				CustomContainer container = createContainer(files[i], parent);
				parent.addContainer(container);
				parent.setChildCount(i);
				File[] children = files[i].listFiles();
				if (children != null && children.length >= 1) {
					addFiles(children, container);
				}
			}
		}
	}

	public boolean removeItemsFormPath(String path) {
		boolean removeSuccess = false;
		File file = new File(path);
		if (file.isFile() || file.isDirectory()) {
			DIDLObject item = content.findObjectWithId(createId(path));
			if (item == null)
				return false;
			String parentId = item.getParentID();
			Container parent = (Container) content.findObjectWithId(parentId);
			removeSuccess = parent.getItems().remove(item);
			if (!removeSuccess) {
				removeSuccess = parent.getContainers().remove(item);
			}
			parent.setChildCount(parent.getChildCount() - 1);
		}
		return removeSuccess;
	}

	private CustomItem createItem(File file) {
		long size = file.length();
		String title = file.getName();
		String id = createId(file.getPath());
		String path = file.getPath();
		return new CustomItem(id, title, size, path);
	}

	private String createId(String path) {
		try {
			return URLEncoder.encode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	private CustomContainer createContainer(File file, Container parent) {
		return new CustomContainer(parent, createId(file.getPath()),
				file.getName());
	}

	public void addPhotosContainerAndUpdate(Uri uri) {
		if (photosAdded)
			return;
		photosAdded = true;
		PhotosContainer photos = new PhotosContainer(this);
		addContainer(photos);
		photos.update(uri);
		childCount++;
		setChildCount(childCount);
	}

	public void addMusicsContainerAndUpdate(Uri uri) {
		if (musicsAdded)
			return;
		musicsAdded = true;
		MusicsContainer musics = new MusicsContainer(this);
		addContainer(musics);
		musics.update(uri);
		childCount++;
		setChildCount(childCount);
	}

	public void addMoviesContainerAndUpdate(Uri uri) {
		if (moviesAdded)
			return;
		moviesAdded = true;
		MoviesContainer movies = new MoviesContainer(this);
		addContainer(movies);
		movies.update(uri);
		childCount++;
		setChildCount(childCount);
	}

}
