package com.jcn.dlna.sdk.dms.content;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.net.Uri;
import android.provider.MediaStore;

public class MediaStoreContent extends DIDLContent {

	private static final Logger log = Logger.getLogger(MediaStoreContent.class
			.getSimpleName());

	public static final String CREATOR = "System";
	public static final DIDLObject.Class CLASS_CONTAINER = new DIDLObject.Class(
			"object.container");

	private boolean shareEnable = true;

	private MediaStoreContent() {
		RootContainer root = new RootContainer(this);
		addContainer(root);
	}

	private static MediaStoreContent instance;

	public static MediaStoreContent getInstance() {
		if (instance == null) {
			instance = new MediaStoreContent();
		}
		return instance;
	}

	public void setShareEnable(boolean enable) {
		shareEnable = enable;
	}

	public boolean isShareEnable() {
		return shareEnable;
	}

	public boolean addShareFilePath(String path) {
		return getRootContainer().addOtherContainerByPath(path);
	}

	public boolean removeShareFilePath(String path) {
		return getRootContainer().removeItemsFormPath(path);
	}

	public boolean removeAllShareFile() {
		setContainers(new ArrayList<Container>());
		RootContainer root = new RootContainer(this);
		addContainer(root);
		return true;
	}

	public void shareExternalPhotos() {
		updatePhotos(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	}

	public void shareExternalMusics() {
		updateMusics(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
	}

	public void shareExternalMovies() {
		updateMovies(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
	}

	public void shareAllExternalMedia() {
		updateAllContentByExternalUrl();
	}

	public DIDLObject findObjectWithId(String id) {
		for (Container container : getContainers()) {
			if (container.getId().equals(id))
				return container;
			DIDLObject obj = findObjectWithId(id, container);
			if (obj != null)
				return obj;
		}
		return null;
	}

	protected DIDLObject findObjectWithId(String id, Container current) {
		for (Container container : current.getContainers()) {
			if (container.getId().equals(id))
				return container;
			DIDLObject obj = findObjectWithId(id, container);
			if (obj != null)
				return obj;
		}
		for (Item item : current.getItems()) {
			if (item.getId().equals(id))
				return item;
		}
		return null;
	}

	public RootContainer getRootContainer() {
		return (RootContainer) getContainers().get(0);
	}

	public void updateAllContentByExternalUrl() {
		updateMovies(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
		updateMusics(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
		updatePhotos(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		log.info("content update finish.");
	}

	public void updateMovies(Uri uri) {
		this.getRootContainer().addMoviesContainerAndUpdate(uri);
	}

	public void updatePhotos(Uri uri) {
		this.getRootContainer().addPhotosContainerAndUpdate(uri);
	}

	public void updateMusics(Uri uri) {
		this.getRootContainer().addMusicsContainerAndUpdate(uri);
	}
}
