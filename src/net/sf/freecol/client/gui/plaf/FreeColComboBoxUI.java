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

package net.sf.freecol.client.gui.plaf;

import javax.swing.JComponent;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalComboBoxUI;

/**
 * Sets the default opaque attribute to <i>false</i>.
 */
public class FreeColComboBoxUI extends MetalComboBoxUI {

	/**
	 * Creates the UI.
	 *
	 * @param c
	 *            the c
	 * @return the component UI
	 */
	public static ComponentUI createUI(@SuppressWarnings("unused") JComponent c) {
		return new FreeColComboBoxUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.plaf.basic.BasicComboBoxUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);

		c.setOpaque(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicComboBoxUI#createRenderer()
	 */
	@Override
	protected ListCellRenderer createRenderer() {
		return new FreeColComboBoxRenderer();
	}
}
