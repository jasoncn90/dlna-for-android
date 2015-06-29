package com.jcn.dlna.sdk.dms.mediaserver;

import java.util.logging.Logger;

import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.ContentDirectoryException;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.BrowseResult;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import com.jcn.dlna.sdk.dms.content.MediaStoreContent;

public class ContentDirectory extends AbstractContentDirectoryService {

	private static final Logger log = Logger.getLogger(ContentDirectory.class
			.getSimpleName());

	private MediaStoreContent content;

	public ContentDirectory() {
		content = MediaStoreContent.getInstance();
	}

	@Override
	public BrowseResult browse(String objectID, BrowseFlag browseFlag,
			String filter, long firstResult, long maxResults,
			SortCriterion[] orderby) throws ContentDirectoryException {
		log.info("browseId --> " + objectID);
		log.info("browseFlag --> " + browseFlag);

		DIDLContent didl = new DIDLContent();
		try {
			if (!content.isShareEnable()) {
				log.info("dms will not share anything.");
				return new BrowseResult(new DIDLParser().generate(didl), 0, 0);
			}
			DIDLObject object = content.findObjectWithId(objectID);
			if (object == null) {
				log.info("object not found:" + objectID);
				return new BrowseResult(new DIDLParser().generate(didl), 0, 0);
			}
			log.info("find object. id-->" + object.getId() + " title-->"
					+ object.getTitle());
			int count = 0;
			int totalMatches = 0;
			if (browseFlag.equals(BrowseFlag.METADATA)) {
				if (object instanceof Container) {
					log.info("Browsing metadata of container: "
							+ object.getId());
					didl.addContainer((Container) object);
					count++;
					totalMatches++;
				} else if (object instanceof Item) {
					log.info("Browsing metadata of item: " + object.getId());
					didl.addItem((Item) object);
					count++;
					totalMatches++;
				}
			} else if (browseFlag.equals(BrowseFlag.DIRECT_CHILDREN)) {
				if (object instanceof Container) {
					log.info("Browsing children of container: "
							+ object.getId());
					Container container = (Container) object;
					boolean maxReached = maxResults == 0;
					totalMatches = totalMatches
							+ container.getContainers().size();
					for (Container subContainer : container.getContainers()) {
						if (maxReached)
							break;
						if (firstResult > 0 && count == firstResult)
							continue;
						didl.addContainer(subContainer);
						count++;
						if (count >= maxResults)
							maxReached = true;
					}
					totalMatches = totalMatches + container.getItems().size();
					for (Item item : container.getItems()) {
						if (maxReached)
							break;
						if (firstResult > 0 && count == firstResult)
							continue;
						didl.addItem(item);
						count++;
						if (count >= maxResults)
							maxReached = true;
					}
				}
			}
			log.info("Browsing result count: " + count + " and total matches: "
					+ totalMatches);
			return new BrowseResult(new DIDLParser().generate(didl), count,
					totalMatches);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
