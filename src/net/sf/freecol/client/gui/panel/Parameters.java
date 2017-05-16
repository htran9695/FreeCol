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

package net.sf.freecol.client.gui.panel;

/**
 * The Class Parameters.
 */
public class Parameters {

	/** The dist to land from high seas. */
	public final int distToLandFromHighSeas;

	/** The max distance to edge. */
	public final int maxDistanceToEdge;

	/**
	 * Instantiates a new parameters.
	 *
	 * @param distToLandFromHighSeas
	 *            the dist to land from high seas
	 * @param maxDistanceToEdge
	 *            the max distance to edge
	 */
	Parameters(int distToLandFromHighSeas, int maxDistanceToEdge) {
		this.distToLandFromHighSeas = distToLandFromHighSeas;
		this.maxDistanceToEdge = maxDistanceToEdge;
	}
}
