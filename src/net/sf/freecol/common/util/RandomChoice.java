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

package net.sf.freecol.common.util;

import java.util.Collection;
import java.util.Random;
import java.util.logging.Logger;

import static net.sf.freecol.common.util.RandomUtils.*;

/**
 * A class to provide flat and weighted random selection from a collection.
 *
 * @param <T>
 *            the generic type
 */
public class RandomChoice<T> {

	/** The probability. */
	private final int probability;

	/** The object. */
	private final T object;

	/**
	 * Instantiates a new random choice.
	 *
	 * @param object
	 *            the object
	 * @param probability
	 *            the probability
	 */
	public RandomChoice(T object, int probability) {
		this.probability = probability;
		this.object = object;
	}

	/**
	 * Gets the probability.
	 *
	 * @return the probability
	 */
	public int getProbability() {
		return probability;
	}

	/**
	 * Gets the object.
	 *
	 * @return the object
	 */
	public T getObject() {
		return object;
	}

	/**
	 * Select.
	 *
	 * @param <T>
	 *            the generic type
	 * @param input
	 *            the input
	 * @param probability
	 *            the probability
	 * @return the t
	 */
	private static <T> T select(Collection<RandomChoice<T>> input, int probability) {
		if (input.isEmpty())
			return null;

		int total = 0;
		for (RandomChoice<T> choice : input) {
			total += choice.getProbability();
			if (probability < total)
				return choice.getObject();
		}
		return input.iterator().next().getObject();
	}

	/**
	 * Gets the total probability.
	 *
	 * @param <T>
	 *            the generic type
	 * @param input
	 *            the input
	 * @return the total probability
	 */
	public static <T> int getTotalProbability(Collection<RandomChoice<T>> input) {
		return input.stream().mapToInt(c -> c.getProbability()).sum();
	}

	/**
	 * Gets the weighted random.
	 *
	 * @param <T>
	 *            the generic type
	 * @param logger
	 *            the logger
	 * @param logMe
	 *            the log me
	 * @param input
	 *            the input
	 * @param random
	 *            the random
	 * @return the weighted random
	 */
	public static <T> T getWeightedRandom(Logger logger, String logMe, Collection<RandomChoice<T>> input,
			Random random) {
		int n;
		return (input == null || input.isEmpty() || (n = getTotalProbability(input)) <= 0) ? null
				: (input.size() == 1) ? input.iterator().next().getObject()
						: select(input, randomInt(logger, logMe, random, n));
	}
}
