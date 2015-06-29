package com.jcn.dlna.sdk.dms.content;

import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Photo;
import org.teleal.common.util.MimeType;

import com.jcn.dlna.sdk.dms.UrlBuilder;

public class OtherContainer extends Container {

	public static final String ID = "other";

	public OtherContainer(RootContainer rootContainer) {
		setId(ID);
		setParentID(rootContainer.getId());
		setTitle("Other");
		setCreator(MediaStoreContent.CREATOR);
		setClazz(MediaStoreContent.CLASS_CONTAINER);
		setRestricted(true);
		setSearchable(false);
		setWriteStatus(WriteStatus.NOT_WRITABLE);
	}

	public static class CustomContainer extends Container {
		public CustomContainer(Container parent, String id, String title) {
			setId(id);
			setParentID(parent.getId());
			setTitle(title);
			setCreator(MediaStoreContent.CREATOR);
			setClazz(MediaStoreContent.CLASS_CONTAINER);
			setRestricted(true);
			setSearchable(false);
			setWriteStatus(WriteStatus.NOT_WRITABLE);
		}
	}

	public static class CustomItem extends Photo {

		private String path;
		private String id;

		public CustomItem(String id, String title, long size, String path) {
			this.path = path;
			this.id = id;
			setId(id);
			setParentID(OtherContainer.ID);
			setTitle(title);
			setCreator(MediaStoreContent.CREATOR);

			Res resource = new Res() {
				@Override
				public String getValue() {
					return UrlBuilder.createUrlByCustomItem(CustomItem.this);
				}
			};
			resource.setProtocolInfo(new ProtocolInfo(new MimeType()));
			resource.setSize(size);
			addResource(resource);
		}

		public String getPath() {
			return path;
		}

		@Override
		public String getId() {
			return id;
		}
	}
}
