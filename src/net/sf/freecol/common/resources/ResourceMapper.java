/**
 *  Copyright (C) 2002-2015   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.resources;

/**
 * Puts the Resource into the ResourceMapping.
 */
public final class ResourceMapper implements ResourceFactory.ResourceSink {

	/** The resource mapping. */
	private final ResourceMapping resourceMapping;

	/** The key. */
	private String key;

	/**
	 * Instantiates a new resource mapper.
	 *
	 * @param resourceMapping
	 *            the resource mapping
	 */
	public ResourceMapper(ResourceMapping resourceMapping) {
		this.resourceMapping = resourceMapping;
		key = null;
	}

	/**
	 * Sets the key.
	 *
	 * @param key
	 *            the new key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.freecol.common.resources.ResourceFactory.ResourceSink#add(net.sf.
	 * freecol.common.resources.ColorResource)
	 */
	@Override
	public void add(ColorResource r) {
		resourceMapping.add(key, r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.freecol.common.resources.ResourceFactory.ResourceSink#add(net.sf.
	 * freecol.common.resources.FontResource)
	 */
	@Override
	public void add(FontResource r) {
		resourceMapping.add(key, r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.freecol.common.resources.ResourceFactory.ResourceSink#add(net.sf.
	 * freecol.common.resources.StringResource)
	 */
	@Override
	public void add(StringResource r) {
		resourceMapping.add(key, r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.freecol.common.resources.ResourceFactory.ResourceSink#add(net.sf.
	 * freecol.common.resources.FAFileResource)
	 */
	@Override
	public void add(FAFileResource r) {
		resourceMapping.add(key, r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.freecol.common.resources.ResourceFactory.ResourceSink#add(net.sf.
	 * freecol.common.resources.SZAResource)
	 */
	@Override
	public void add(SZAResource r) {
		resourceMapping.add(key, r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.freecol.common.resources.ResourceFactory.ResourceSink#add(net.sf.
	 * freecol.common.resources.AudioResource)
	 */
	@Override
	public void add(AudioResource r) {
		resourceMapping.add(key, r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.freecol.common.resources.ResourceFactory.ResourceSink#add(net.sf.
	 * freecol.common.resources.VideoResource)
	 */
	@Override
	public void add(VideoResource r) {
		resourceMapping.add(key, r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.freecol.common.resources.ResourceFactory.ResourceSink#add(net.sf.
	 * freecol.common.resources.ImageResource)
	 */
	@Override
	public void add(ImageResource r) {
		resourceMapping.add(key, r);
	}

}
