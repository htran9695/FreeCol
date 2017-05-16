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

package net.sf.freecol.tools;

import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * The Class ColonizationSaveGameReader.
 */
public class ColonizationSaveGameReader {

	/** The Constant PLAYER_DATA. */
	private final static int PLAYER_DATA = 0x9e;

	/** The Constant COLONY_DATA. */
	private final static int COLONY_DATA = 0x186;

	/** The Constant NATIONS. */
	private final static String[] NATIONS = { "English", "French", "Spanish", "Dutch" };

	/**
	 * The Class GameData.
	 */
	private class GameData {

		/** The map width. */
		private final int mapWidth;

		/** The map height. */
		private final int mapHeight;

		/** The number of colonies. */
		private final int numberOfColonies;

		/** The difficulty. */
		private final int difficulty;

		/**
		 * Instantiates a new game data.
		 *
		 * @param data
		 *            the data
		 */
		public GameData(byte[] data) {
			mapWidth = data[0xc];
			mapHeight = data[0xe];
			numberOfColonies = data[0x2e];
			difficulty = data[0x36];
		}

		/**
		 * Prints the.
		 */
		public void print() {
			System.out.println("Map size is " + mapWidth + " x " + mapHeight);
			System.out.println("Difficulty is " + difficulty);
			System.out.println(numberOfColonies + " colonies found");
		}

		/**
		 * Gets the number of colonies.
		 *
		 * @return the number of colonies
		 */
		public int getNumberOfColonies() {
			return numberOfColonies;
		}
	}

	/**
	 * The Class PlayerData.
	 */
	private class PlayerData {

		/** The Constant LENGTH. */
		public static final int LENGTH = 52;

		/** The new land name. */
		private final String newLandName;

		/** The player name. */
		private final String playerName;

		/** The human player. */
		private final boolean humanPlayer;

		/**
		 * Instantiates a new player data.
		 *
		 * @param data
		 *            the data
		 * @param offset
		 *            the offset
		 */
		public PlayerData(byte[] data, int offset) {
			playerName = getString(data, offset, 23);
			newLandName = getString(data, offset + 24, 23);
			humanPlayer = (data[offset + 49] == 0);
		}

		/**
		 * Prints the.
		 */
		public void print() {
			System.out.println("Player name is " + playerName + (humanPlayer ? " [human]" : " [AI]"));
			System.out.println("New land name is " + newLandName);
		}

	}

	/**
	 * The Class ColonyData.
	 */
	private class ColonyData {

		/** The Constant LENGTH. */
		public static final int LENGTH = 202;

		/** The Constant COLONIST_OCCUPATION. */
		public static final int COLONIST_OCCUPATION = 0x20;

		/** The Constant COLONIST_SPECIALITY. */
		public static final int COLONIST_SPECIALITY = 0x40;

		/** The Constant TILES. */
		public static final int TILES = 0x70;

		/** The x. */
		private final int x;

		/** The y. */
		private final int y;

		/** The number of colonists. */
		private final int numberOfColonists;

		/** The name. */
		private final String name;

		/** The colonists. */
		private final Colonist[] colonists;

		/**
		 * Instantiates a new colony data.
		 *
		 * @param data
		 *            the data
		 * @param offset
		 *            the offset
		 */
		public ColonyData(byte[] data, int offset) {
			x = data[offset];
			y = data[offset + 1];
			name = getString(data, offset + 2, offset + 25);
			numberOfColonists = data[offset + 0x1f];
			colonists = new Colonist[numberOfColonists];
			for (int index = 0; index < numberOfColonists; index++) {
				int tile = -1;
				for (int tileIndex = 0; tileIndex < 8; tileIndex++) {
					if (data[offset + TILES + tileIndex] == index) {
						tile = tileIndex;
						break;
					}
				}
				colonists[index] = new Colonist(data[offset + COLONIST_OCCUPATION + index],
						data[offset + COLONIST_SPECIALITY + index], tile);
			}

		}

		/**
		 * Prints the.
		 */
		public void print() {
			System.out.println(name + " [" + x + ", " + y + "], " + numberOfColonists + " colonists");
			for (Colonist colonist : colonists) {
				colonist.print();
			}
		}
	}

	/**
	 * The Class Colonist.
	 */
	public class Colonist {

		/** The occupation. */
		public final String[] OCCUPATION = { "Farmer", "Sugar planter", "Tobacco planter", "Cotton planter",
				"Fur trapper", "Lumberjack", "Ore miner", "Silver miner", "Fisherman", "Distiller", "Tobacconist",
				"Weaver", "Fur Trader", "Carpenter", "Blacksmith", "Gunsmith", "Preacher", "Statesman", "Teacher", "",
				"Pioneer", "Veteran Soldier", "Scout", "Veteran Dragoon", "Missionary", "Indentured Servant",
				"Petty Criminal", "Indian convert", "Free colonist", "Armed brave", "Mounted brave" };

		/** The tiles. */
		public final String[] TILES = { "N", "E", "S", "W", "NW", "NE", "SE", "SW" };

		/** The occupation. */
		final int occupation;

		/** The speciality. */
		final int speciality;

		/** The tile. */
		final int tile;

		/**
		 * Instantiates a new colonist.
		 *
		 * @param occupation
		 *            the occupation
		 * @param speciality
		 *            the speciality
		 * @param tile
		 *            the tile
		 */
		public Colonist(int occupation, int speciality, int tile) {
			this.occupation = occupation;
			this.speciality = speciality;
			this.tile = tile;
		}

		/**
		 * Prints the.
		 */
		public void print() {
			String tileString = (tile >= 0) ? " [tile " + TILES[tile] + "]" : "";
			System.out.println(OCCUPATION[speciality] + " working as " + OCCUPATION[occupation] + tileString);
		}

	}

	/** The data. */
	private final byte[] data;

	/**
	 * Instantiates a new colonization save game reader.
	 *
	 * @param data
	 *            the data
	 */
	public ColonizationSaveGameReader(byte[] data) {
		this.data = data;
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String[] args) throws Exception {

		byte[] data;
		try (RandomAccessFile reader = new RandomAccessFile(args[0], "r")) {
			data = new byte[(int) reader.length()];
			reader.read(data);
		}
		new ColonizationSaveGameReader(data).run();
	}

	/**
	 * Run.
	 */
	private void run() {

		GameData gameData = new GameData(data);
		gameData.print();
		for (int index = 0; index < 4; index++) {
			System.out.println("Nation is " + NATIONS[index]);
			PlayerData playerData = new PlayerData(data, PLAYER_DATA + index * PlayerData.LENGTH);
			playerData.print();
		}
		int count = gameData.getNumberOfColonies();
		for (int index = 0; index < count; index++) {
			ColonyData colonyData = new ColonyData(data, COLONY_DATA + index * ColonyData.LENGTH);
			colonyData.print();
		}

	}

	/**
	 * Gets the string.
	 *
	 * @param data
	 *            the data
	 * @param start
	 *            the start
	 * @param length
	 *            the length
	 * @return the string
	 */
	public static String getString(byte[] data, int start, int length) {
		byte[] bytes = Arrays.copyOfRange(data, start, start + length);
		String value = new String(bytes);
		int index = value.indexOf(0);
		if (index < 0) {
			return value;
		} else {
			return value.substring(0, index);
		}
	}
}