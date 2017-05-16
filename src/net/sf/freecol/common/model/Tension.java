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

package net.sf.freecol.common.model;

import net.sf.freecol.common.i18n.Messages;
import static net.sf.freecol.common.util.CollectionUtils.*;
import static net.sf.freecol.common.util.StringUtils.*;

/**
 * A measure of the tension between nations.
 */
public class Tension implements Named {

	/** The Constant TENSION_MIN. */
	// Minimum possible tension value.
	public static final int TENSION_MIN = 0;

	/** The Constant TENSION_MAX. */
	// Maximum possible tension value.
	public static final int TENSION_MAX = Level.HATEFUL.limit + 100;

	/** The Constant DELTA. */
	// Hysteresis value
	public static final int DELTA = 10;

	/** The Constant TENSION_ADD_MINOR. */
	// Unit destroyed, etc
	public static final int TENSION_ADD_MINOR = 100;

	/** The Constant TENSION_ADD_NORMAL. */
	// Unit destroyed in a Settlement, etc
	public static final int TENSION_ADD_NORMAL = 200;

	/** The Constant TENSION_ADD_MAJOR. */
	// Unit destroyed in a capital, etc
	public static final int TENSION_ADD_MAJOR = 300;

	/** The Constant TENSION_ADD_LAND_TAKEN. */
	// Grab land without paying
	public static final int TENSION_ADD_LAND_TAKEN = 200;

	/** The Constant TENSION_ADD_UNIT_DESTROYED. */
	// Unit destroyed
	public static final int TENSION_ADD_UNIT_DESTROYED = 400;

	/** The Constant TENSION_ADD_SETTLEMENT_ATTACKED. */
	// Settlement attacked
	public static final int TENSION_ADD_SETTLEMENT_ATTACKED = 500;

	/** The Constant TENSION_ADD_CAPITAL_ATTACKED. */
	// Capital attacked
	public static final int TENSION_ADD_CAPITAL_ATTACKED = 600;

	/** The Constant TENSION_ADD_WAR_INCITER. */
	// War inciter
	public static final int TENSION_ADD_WAR_INCITER = 250;

	/** The Constant CONTACT_MODIFIER. */
	// Tension modifiers
	public static final int CONTACT_MODIFIER = 0;

	/** The Constant ALLIANCE_MODIFIER. */
	public static final int ALLIANCE_MODIFIER = -500;

	/** The Constant DROP_ALLIANCE_MODIFIER. */
	public static final int DROP_ALLIANCE_MODIFIER = 200;

	/** The Constant PEACE_TREATY_MODIFIER. */
	public static final int PEACE_TREATY_MODIFIER = -250;

	/** The Constant CEASE_FIRE_MODIFIER. */
	public static final int CEASE_FIRE_MODIFIER = -250;

	/** The Constant WAR_MODIFIER. */
	public static final int WAR_MODIFIER = Level.HATEFUL.limit;

	/** The Constant RESUME_WAR_MODIFIER. */
	public static final int RESUME_WAR_MODIFIER = 750; // War from cease fire

	/** Tension level to set when surrendering. */
	public static final int SURRENDERED = (Level.CONTENT.limit + Level.HAPPY.limit) / 2;

	/**
	 * Constants for describing alarm levels.
	 */
	public static enum Level {

		/** The happy. */
		HAPPY(100),

		/** The content. */
		CONTENT(600),

		/** The displeased. */
		DISPLEASED(700),

		/** The angry. */
		ANGRY(800),

		/** The hateful. */
		HATEFUL(1000);

		/** The limit. */
		private final int limit;

		/**
		 * Instantiates a new level.
		 *
		 * @param limit
		 *            the limit
		 */
		Level(int limit) {
			this.limit = limit;
		}

		/**
		 * Gets the limit.
		 *
		 * @return the limit
		 */
		public int getLimit() {
			return limit;
		}

		/**
		 * Get a message key for the level.
		 *
		 * @return A message key.
		 */
		public String getKey() {
			return getEnumKey(this);
		}
	}

	/** The value. */
	private int value;

	/**
	 * Create the default tension.
	 */
	public Tension() {
		setValue(Level.HAPPY.getLimit());
	}

	/**
	 * Create tension at a specified level.
	 *
	 * @param newTension
	 *            The level of tension.
	 */
	public Tension(int newTension) {
		setValue(newTension);
	}

	/**
	 * Get the current tension value.
	 *
	 * @return The value of this <code>Tension</code>.
	 */
	public final int getValue() {
		return this.value;
	}

	/**
	 * Sets the current tension value.
	 *
	 * @param newValue
	 *            The new value of the tension.
	 */
	public final void setValue(int newValue) {
		if (newValue < TENSION_MIN) {
			value = TENSION_MIN;
		} else if (newValue > TENSION_MAX) {
			value = TENSION_MAX;
		} else {
			value = newValue;
		}
	}

	/**
	 * Get the current tension level.
	 *
	 * @return The current level.
	 */
	public final Level getLevel() {
		return find(Level.values(), level -> value <= level.getLimit(), Level.HATEFUL);
	}

	/**
	 * Modify the tension by the given amount.
	 *
	 * @param newTension
	 *            The amount to modify tension by.
	 */
	public final void modify(int newTension) {
		setValue(value + newTension);
	}

	/**
	 * Get the stem key.
	 *
	 * @return The tension stem key.
	 */
	public String getKey() {
		return "tension." + getLevel().getKey();
	}

	// Implement Named

	/**
	 * {@inheritDoc}
	 */
	public String getNameKey() {
		return Messages.nameKey("model." + getKey());
	}

	// Override Object

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getLevel().toString();
	}
}
