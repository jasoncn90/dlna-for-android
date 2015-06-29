package com.jcn.dlna.sdk.dms.content;

import org.teleal.common.util.MimeType;

public interface MediaStoreItem {

	public long getMediaStoreId();

	public MimeType getMimeType();

	public long getSize();

	public String getPath();
}
