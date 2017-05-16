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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.common.i18n.Messages;
import net.sf.freecol.common.model.Ability;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.Location;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.WorkLocation;

/**
 * Holds labour statistics for the labour report.
 */
public class LabourData {

	/** The Constant UNITS_IN_EUROPE_GETTER. */
	private static final LocationData.Getter UNITS_IN_EUROPE_GETTER = new LocationData.Getter() {
		@Override
		public LocationData getLocationData(UnitData unitData) {
			return unitData.unitsInEurope;
		}
	};

	/** The Constant UNITS_AT_SEA_GETTER. */
	private static final LocationData.Getter UNITS_AT_SEA_GETTER = new LocationData.Getter() {
		@Override
		public LocationData getLocationData(UnitData unitData) {
			return unitData.unitsAtSea;
		}
	};

	/** The Constant UNITS_ON_LAND_GETTER. */
	private static final LocationData.Getter UNITS_ON_LAND_GETTER = new LocationData.Getter() {
		@Override
		public LocationData getLocationData(UnitData unitData) {
			return unitData.unitsOnLand;
		}
	};

	/**
	 * The Class ProductionData.
	 */
	public static class ProductionData {

		/** number of colonists. */
		private int colonists;

		/** Potential production. */
		private int production;

		/**
		 * Adds the production.
		 *
		 * @param production
		 *            the production
		 */
		public void addProduction(int production) {
			colonists = getColonists() + 1;
			this.production = this.getProduction() + production;
		}

		/**
		 * Gets the colonists.
		 *
		 * @return the colonists
		 */
		public int getColonists() {
			return colonists;
		}

		/**
		 * Gets the production.
		 *
		 * @return the production
		 */
		public int getProduction() {
			return production;
		}

		/**
		 * Adds the.
		 *
		 * @param data
		 *            the data
		 */
		private void add(ProductionData data) {
			colonists += data.colonists;
			production += data.production;
		}
	}

	/**
	 * The Class LocationData.
	 */
	public static class LocationData {

		/**
		 * The Interface Getter.
		 */
		public interface Getter {

			/**
			 * Gets the location data.
			 *
			 * @param unitData
			 *            the unit data
			 * @return the location data
			 */
			LocationData getLocationData(UnitData unitData);
		}

		/** associated unit data. */
		private final UnitData unitData;

		/** if this is the total for the unit data. */
		private final boolean isTotal;

		/** experts working in their expert field. */
		private final ProductionData workingProfessionals = new ProductionData();

		/** lumberjacks working as something else. */
		private int workingAmateurs;

		/** others working as lumberjacks. */
		private final ProductionData otherWorkingAmateurs = new ProductionData();

		/** net production of goods. */
		private int netProduction;

		/** teachers. */
		private int teachers;

		/**
		 * students learning this job (i.e. lumberjacks, not free colonists)
		 */
		private int otherStudents;

		/** of the other students. */
		private String otherStudentsName;

		/**
		 * students in their old type (i.e. free colonists, not lumberjacks)
		 */
		private int students;

		/** not working colonists. */
		private int notWorking;

		/**
		 * Instantiates a new location data.
		 *
		 * @param unitData
		 *            the unit data
		 */
		public LocationData(UnitData unitData) {
			this(unitData, false);
		}

		/**
		 * Instantiates a new location data.
		 *
		 * @param unitData
		 *            the unit data
		 * @param total
		 *            the total
		 */
		public LocationData(UnitData unitData, boolean total) {
			this.unitData = unitData;
			isTotal = total;
		}

		/**
		 * Gets the other students.
		 *
		 * @return the other students
		 */
		public int getOtherStudents() {
			return otherStudents;
		}

		/**
		 * Gets the other students name.
		 *
		 * @return the other students name
		 */
		public String getOtherStudentsName() {
			return otherStudentsName;
		}

		/**
		 * Adds the other student.
		 *
		 * @param name
		 *            the name
		 */
		public void addOtherStudent(String name) {
			otherStudents++;
			otherStudentsName = name;
		}

		/**
		 * Gets the working professionals.
		 *
		 * @return the working professionals
		 */
		public ProductionData getWorkingProfessionals() {
			return workingProfessionals;
		}

		/**
		 * Gets the other working amateurs.
		 *
		 * @return the other working amateurs
		 */
		public ProductionData getOtherWorkingAmateurs() {
			return otherWorkingAmateurs;
		}

		/**
		 * Gets the net production.
		 *
		 * @return the net production
		 */
		public int getNetProduction() {
			return netProduction;
		}

		/**
		 * Gets the total colonists.
		 *
		 * @return the total colonists
		 */
		public int getTotalColonists() {
			// count as if the unit was already teached, this makes teaching
			// easier to plan
			// other working amateurs are not counted per default
			return workingAmateurs + workingProfessionals.getColonists() + notWorking + teachers + otherStudents
					- students;
		}

		/**
		 * Gets the total production.
		 *
		 * @return the total production
		 */
		public int getTotalProduction() {
			return workingProfessionals.getProduction() + otherWorkingAmateurs.getProduction();
		}

		/**
		 * in the summary for all unit types, some rows are skipped.
		 *
		 * @return the rows to display the unit data
		 */
		public int getRowCount() {
			boolean isSummary = getUnitData().isSummary();

			int rows = 0;
			if (workingProfessionals.getColonists() > 0)
				rows++;
			if (workingAmateurs > 0)
				rows++;
			if (!isSummary && otherWorkingAmateurs.getColonists() > 0)
				rows++;
			if (teachers > 0)
				rows++;
			if (students > 0)
				rows++;
			if (!isSummary && otherStudents > 0)
				rows++;
			if (notWorking > 0)
				rows++;

			return rows;
		}

		/**
		 * Checks if is training.
		 *
		 * @return true, if is training
		 */
		public boolean isTraining() {
			return teachers > 0 || students > 0 || otherStudents > 0;
		}

		/**
		 * Gets the working amateurs.
		 *
		 * @return the working amateurs
		 */
		public int getWorkingAmateurs() {
			return workingAmateurs;
		}

		/**
		 * Gets the teachers.
		 *
		 * @return the teachers
		 */
		public int getTeachers() {
			return teachers;
		}

		/**
		 * Gets the students.
		 *
		 * @return the students
		 */
		public int getStudents() {
			return students;
		}

		/**
		 * Gets the not working.
		 *
		 * @return the not working
		 */
		public int getNotWorking() {
			return notWorking;
		}

		/**
		 * Gets the unit data.
		 *
		 * @return the unit data
		 */
		public UnitData getUnitData() {
			return unitData;
		}

		/**
		 * Checks if is total.
		 *
		 * @return true, if is total
		 */
		public boolean isTotal() {
			return isTotal;
		}

		/**
		 * Adds the.
		 *
		 * @param data
		 *            the data
		 */
		private void add(LocationData data) {
			workingProfessionals.add(data.workingProfessionals);
			workingAmateurs += data.workingAmateurs;
			otherWorkingAmateurs.add(data.otherWorkingAmateurs);

			teachers += data.teachers;
			students += data.students;
			otherStudents += data.otherStudents;
			notWorking += data.notWorking;

			if (data.otherStudents > 0) {
				otherStudentsName = data.otherStudentsName;
			}
			// net production is calculated separately
		}
	}

	/**
	 * The Class UnitData.
	 */
	public static class UnitData {

		/** The unit type. */
		private final UnitType unitType;

		/** The summary. */
		private boolean summary = false;

		/** Map[Colony, colony details]]. */
		private final Map<Colony, LocationData> details = new LinkedHashMap<>();

		/** The total. */
		private final LocationData total = new LocationData(this, true);

		/** The units at sea. */
		private final LocationData unitsAtSea = new LocationData(this);

		/** The units on land. */
		private final LocationData unitsOnLand = new LocationData(this);

		/** The units in europe. */
		private final LocationData unitsInEurope = new LocationData(this);

		/**
		 * Instantiates a new unit data.
		 *
		 * @param unitType
		 *            the unit type
		 */
		public UnitData(UnitType unitType) {
			this.unitType = unitType;

			if (unitType == null) {
				summary = true;
			}
		}

		/**
		 * get labour data (create on demand).
		 *
		 * @param colony
		 *            the colony
		 * @return labour data
		 */
		private LocationData getLocationData(Colony colony) {
			LocationData colonyData = details.get(colony);
			if (colonyData == null) {
				colonyData = new LocationData(this);
				details.put(colony, colonyData);
			}
			return colonyData;
		}

		/**
		 * Gets the unit name.
		 *
		 * @return the unit name
		 */
		public String getUnitName() {
			return (isSummary()) ? null : Messages.getName(unitType);
		}

		/**
		 * Checks for details.
		 *
		 * @return true, if successful
		 */
		public boolean hasDetails() {
			return getTotal().getRowCount() > 0;
		}

		/**
		 * Gets the unit summary row count.
		 *
		 * @return the unit summary row count
		 */
		public int getUnitSummaryRowCount() {
			// minimum 1 row to show the unit symbol
			return Math.max(1, getTotal().getRowCount());
		}

		/**
		 * Gets the unit type.
		 *
		 * @return the unit type
		 */
		public UnitType getUnitType() {
			return unitType;
		}

		/**
		 * Gets the total.
		 *
		 * @return the total
		 */
		public LocationData getTotal() {
			return total;
		}

		/**
		 * Gets the units at sea.
		 *
		 * @return the units at sea
		 */
		public LocationData getUnitsAtSea() {
			return unitsAtSea;
		}

		/**
		 * Gets the units on land.
		 *
		 * @return the units on land
		 */
		public LocationData getUnitsOnLand() {
			return unitsOnLand;
		}

		/**
		 * Gets the units in europe.
		 *
		 * @return the units in europe
		 */
		public LocationData getUnitsInEurope() {
			return unitsInEurope;
		}

		/**
		 * Gets the details.
		 *
		 * @return the details
		 */
		public Map<Colony, LocationData> getDetails() {
			return details;
		}

		/**
		 * Checks if is summary.
		 *
		 * @return true, if is summary
		 */
		public boolean isSummary() {
			return summary;
		}

		/**
		 * Show production.
		 *
		 * @return true, if successful
		 */
		public boolean showProduction() {
			return !summary && unitType.getExpertProduction() != null;
		}

		/**
		 * Show net production.
		 *
		 * @return true, if successful
		 */
		public boolean showNetProduction() {
			return showProduction() && unitType.getExpertProduction().isStorable();
		}

		/**
		 * Gets the expert production.
		 *
		 * @return the expert production
		 */
		public GoodsType getExpertProduction() {
			if (summary) {
				return null;
			}
			return getUnitType().getExpertProduction();
		}
	}

	/** The experts. */
	private final Map<GoodsType, UnitData> experts = new LinkedHashMap<>();

	/** The unit data map. */
	private final Map<String, UnitData> unitDataMap = new LinkedHashMap<>();

	/** The summary. */
	private final UnitData summary = new UnitData(null);

	/** The missionary. */
	private UnitData missionary;

	/** The pioneer. */
	private UnitData pioneer;

	/** The soldier. */
	private UnitData soldier;

	/** The scout. */
	private UnitData scout;

	/**
	 * Instantiates a new labour data.
	 *
	 * @param client
	 *            the client
	 */
	public LabourData(FreeColClient client) {
		Specification spec = client.getGame().getSpecification();
		for (UnitType unitType : spec.getUnitTypeList()) {
			if (!unitType.isAvailableTo(client.getMyPlayer())) {
				continue;
			}
			GoodsType production = unitType.getExpertProduction();
			if (production != null) {
				experts.put(production, getUnitData(unitType));
			}

			if (unitType.hasAbility(Ability.EXPERT_PIONEER)) {
				pioneer = getUnitData(unitType);
			} else if (unitType.hasAbility(Ability.EXPERT_SOLDIER)) {
				soldier = getUnitData(unitType);
			} else if (unitType.hasAbility(Ability.EXPERT_SCOUT)) {
				scout = getUnitData(unitType);
			} else if (unitType.hasAbility(Ability.EXPERT_MISSIONARY)) {
				missionary = getUnitData(unitType);
			}
		}

		gatherData(client.getMyPlayer());
	}

	/**
	 * Gather data.
	 *
	 * @param player
	 *            the player
	 */
	private void gatherData(Player player) {
		List<UnitType> labourTypes = getLabourTypes(player);

		Iterator<Unit> units = player.getUnitIterator();
		while (units.hasNext()) {
			Unit unit = units.next();
			if (!labourTypes.contains(unit.getType())) {
				continue;
			}

			Location location = unit.getLocation();

			UnitData data = getUnitData(unit.getType());

			if (location instanceof WorkLocation) {
				incrementColonyCount(location.getColony(), unit, data);
			} else if (location instanceof Europe) {
				incrementOutsideWorker(data, unit, UNITS_IN_EUROPE_GETTER);
			} else if (location instanceof Tile && ((Tile) location).hasSettlement()) {
				incrementColonyCount((Colony) location.getSettlement(), unit, data);
			} else if (location instanceof Unit) {
				incrementOutsideWorker(data, unit, UNITS_AT_SEA_GETTER);
			} else {
				incrementOutsideWorker(data, unit, UNITS_ON_LAND_GETTER);
			}
		}
		summarize();

		for (UnitData unitData : unitDataMap.values()) {
			LocationData total = unitData.getTotal();

			GoodsType expertProduction = unitData.getUnitType().getExpertProduction();
			if (expertProduction != null) {
				for (Colony colony : player.getColonies()) {
					LocationData data = unitData.details.containsKey(colony) ? unitData.getLocationData(colony) : null;

					int netProduction = colony.getNetProductionOf(expertProduction);
					if (data != null) {
						data.netProduction = netProduction;
					}
					total.netProduction += netProduction;
				}
			}
		}
	}

	/**
	 * Gets the labour types.
	 *
	 * @param player
	 *            the player
	 * @return the labour types
	 */
	public static List<UnitType> getLabourTypes(Player player) {
		return player.getSpecification().getUnitTypeList().stream()
				.filter(ut -> ut.hasSkill() && ut.isAvailableTo(player)).collect(Collectors.toList());
	}

	/**
	 * Summarize.
	 */
	private void summarize() {
		for (UnitData unitData : unitDataMap.values()) {
			summarize(unitData, UNITS_IN_EUROPE_GETTER);
			summarize(unitData, UNITS_AT_SEA_GETTER);
			summarize(unitData, UNITS_ON_LAND_GETTER);

			for (final Colony colony : unitData.details.keySet()) {
				summarize(unitData, new LocationData.Getter() {
					@Override
					public LocationData getLocationData(UnitData data) {
						return data.getLocationData(colony);
					}
				});
			}
		}
	}

	/**
	 * Summarize.
	 *
	 * @param data
	 *            the data
	 * @param getter
	 *            the getter
	 */
	private void summarize(UnitData data, LocationData.Getter getter) {
		LocationData unitLocation = getter.getLocationData(data);
		LocationData summaryLocation = getter.getLocationData(summary);

		data.total.add(unitLocation);
		summaryLocation.add(unitLocation);
		summary.total.add(unitLocation);
	}

	/**
	 * Increment outside worker.
	 *
	 * @param unitData
	 *            the unit data
	 * @param unit
	 *            the unit
	 * @param getter
	 *            the getter
	 */
	private void incrementOutsideWorker(UnitData unitData, Unit unit, LocationData.Getter getter) {
		String roleId = unit.getRole().getId();
		UnitData workingAs = null;
		if (unit.isArmed()) {
			workingAs = soldier;
		} else if (unit.hasAbility(Ability.ESTABLISH_MISSION)) {
			workingAs = missionary;
		} else if (unit.hasAbility(Ability.IMPROVE_TERRAIN)) {
			workingAs = pioneer;
		} else if (unit.hasAbility(Ability.SPEAK_WITH_CHIEF)) {
			workingAs = scout;
		}
		if (workingAs == null) {
			getter.getLocationData(unitData).notWorking++;
		} else {
			incrementOutsideWorker(unitData, unit, workingAs, getter);
		}
	}

	/**
	 * Increment outside worker.
	 *
	 * @param expert
	 *            the expert
	 * @param unit
	 *            the unit
	 * @param workingAs
	 *            the working as
	 * @param getter
	 *            the getter
	 */
	private void incrementOutsideWorker(UnitData expert, Unit unit, UnitData workingAs, LocationData.Getter getter) {
		if (unit.getType() == workingAs.unitType) {
			getter.getLocationData(expert).workingProfessionals.colonists++;
		} else {
			getter.getLocationData(expert).workingAmateurs++;

			getter.getLocationData(workingAs).otherWorkingAmateurs.colonists++;
		}
	}

	/**
	 * Increment colony count.
	 *
	 * @param colony
	 *            the colony
	 * @param unit
	 *            the unit
	 * @param unitData
	 *            the unit data
	 */
	private void incrementColonyCount(final Colony colony, Unit unit, UnitData unitData) {
		if (!unit.isInColony()) {
			incrementOutsideWorker(unitData, unit, new LocationData.Getter() {
				@Override
				public LocationData getLocationData(UnitData data) {
					return data.getLocationData(colony);
				}
			});
			return;
		}
		WorkLocation wl = unit.getWorkLocation();
		LocationData colonyData = unitData.getLocationData(colony);
		Unit teacher = unit.getTeacher();
		if (teacher != null) {
			colonyData.students++;

			UnitData learning = getUnitData(Unit.getUnitTypeTeaching(teacher.getType(), unit.getType()));
			learning.getLocationData(colony).addOtherStudent(unitData.getUnitName());
		}
		if (wl.canTeach())
			colonyData.teachers++;

		GoodsType currentlyWorking = unit.getWorkType();
		int production = (currentlyWorking == null) ? 0 : wl.getProductionOf(unit, currentlyWorking);

		UnitData workingAs = experts.get(currentlyWorking);
		if (workingAs == null) {
			; // pass
		} else if (workingAs.getUnitType() == unit.getType()) {
			colonyData.getWorkingProfessionals().addProduction(production);
		} else {
			colonyData.workingAmateurs++;

			workingAs.getLocationData(colony).otherWorkingAmateurs.addProduction(production);
		}
	}

	/**
	 * get profession data (create on demand).
	 *
	 * @param unitType
	 *            goods unitType
	 * @return profession data
	 */
	public final UnitData getUnitData(UnitType unitType) {
		UnitData data = unitDataMap.get(unitType.getId());
		if (data == null) {
			data = new UnitData(unitType);
			unitDataMap.put(unitType.getId(), data);
		}
		return data;
	}

	/**
	 * Gets the summary.
	 *
	 * @return the summary
	 */
	public UnitData getSummary() {
		return summary;
	}
}
