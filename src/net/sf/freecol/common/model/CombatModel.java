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

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * A abstract requirements for a FreeCol combat model.
 */
public abstract class CombatModel {

	/**
	 * The Enum CombatResult.
	 */
	public static enum CombatResult {

		/** The no result. */
		// Special results that set the sense of the result.
		NO_RESULT,

		/** The lose. */
		LOSE,

		/** The win. */
		WIN,

		/** The autoequip unit. */
		// Specific actions
		AUTOEQUIP_UNIT,
		/** The burn missions. */
		// Defending unit auto-arms
		BURN_MISSIONS,
		/** The capture autoequip. */
		// Defending natives burn attackers missions
		CAPTURE_AUTOEQUIP,
		/** The capture colony. */
		// Winner captures loser auto-equipment
		CAPTURE_COLONY,
		/** The capture convert. */
		// Winning Europeans capture a colony
		CAPTURE_CONVERT,
		/** The capture equip. */
		// Winning Europeans cause native to convert
		CAPTURE_EQUIP,
		/** The capture unit. */
		// Winner captures loser equipment
		CAPTURE_UNIT,
		/** The damage colony ships. */
		// Losing unit is captured
		DAMAGE_COLONY_SHIPS,
		/** The damage ship attack. */
		// Ships in losing colony are damaged
		DAMAGE_SHIP_ATTACK,
		/** The damage ship bombard. */
		// Losing ship is damaged by normal attack
		DAMAGE_SHIP_BOMBARD,
		/** The demote unit. */
		// Losing ship is damaged by bombardment
		DEMOTE_UNIT,
		/** The destroy colony. */
		// Losing unit is demoted
		DESTROY_COLONY,
		/** The destroy settlement. */
		// Winning natives burn a colony
		DESTROY_SETTLEMENT,
		/** The evade attack. */
		// Winner destroys a native settlement
		EVADE_ATTACK,
		/** The evade bombard. */
		// Defending ship evades normal attack
		EVADE_BOMBARD,
		/** The loot ship. */
		// Defending ship evades bombardment
		LOOT_SHIP,
		/** The lose autoequip. */
		// Losing ship is looted
		LOSE_AUTOEQUIP,
		/** The lose equip. */
		// Losing unit auto-arms and loses the arms
		LOSE_EQUIP,
		/** The pillage colony. */
		// Losing unit loses some equipment
		PILLAGE_COLONY,
		/** The promote unit. */
		// Winning natives pillage an undefended colony
		PROMOTE_UNIT,
		/** The sink colony ships. */
		// Winning unit is promoted
		SINK_COLONY_SHIPS,
		/** The sink ship attack. */
		// Ships in losing colony are sunk
		SINK_SHIP_ATTACK,
		/** The sink ship bombard. */
		// Losing ship is sunk by normal attack
		SINK_SHIP_BOMBARD,
		/** The slaughter unit. */
		// Losing ship is sunk by bombardment
		SLAUGHTER_UNIT, // Losing unit is slaughtered
	}

	/**
	 * Odds a particular outcome will occur in combat.
	 */
	public static class CombatOdds {

		/** The Constant UNKNOWN_ODDS. */
		public static final double UNKNOWN_ODDS = -1.0;

		/** The win. */
		public final double win;

		/**
		 * Instantiates a new combat odds.
		 *
		 * @param win
		 *            the win
		 */
		public CombatOdds(double win) {
			this.win = win;
		}
	}

	/**
	 * Empty constructor is sufficient.
	 */
	public CombatModel() {
	}

	/**
	 * Is this just a measurement of offence power?.
	 *
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return True if no defender is provided.
	 */
	public boolean combatIsAttackMeasurement(FreeColGameObject attacker, FreeColGameObject defender) {
		return attacker instanceof Unit && defender == null;
	}

	/**
	 * Is this just a measurement of defence power?.
	 *
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return True if no attacker is provided.
	 */
	public boolean combatIsDefenceMeasurement(FreeColGameObject attacker, FreeColGameObject defender) {
		return attacker == null && defender instanceof Unit;
	}

	/**
	 * Is this combat a normal attack?.
	 *
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return True if the combat is a normal attack.
	 */
	public boolean combatIsAttack(FreeColGameObject attacker, FreeColGameObject defender) {
		return attacker instanceof Unit && defender instanceof Unit;
	}

	/**
	 * Is this combat a attack on a settlement? These happen on the client side
	 * only, for the purposes of the pre-combat display. In these cases the
	 * actual defender unit is hidden from the attacker player, so the defender
	 * is shown as the settlement itself.
	 *
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return True if the combat is a client-side attack on a settlement.
	 */
	public boolean combatIsSettlementAttack(FreeColGameObject attacker, FreeColGameObject defender) {
		return attacker instanceof Unit && defender instanceof Settlement;
	}

	/**
	 * Is this combat a bombardment?.
	 *
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return True if the combat is a bombardment.
	 */
	public boolean combatIsBombard(FreeColGameObject attacker, FreeColGameObject defender) {
		return attacker instanceof Settlement && defender instanceof Unit && ((Unit) defender).isNaval();
	}

	/**
	 * Could this attack be an amphibious operation?.
	 *
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return True if the attack is amphibious.
	 */
	public boolean combatIsAmphibious(FreeColGameObject attacker, FreeColGameObject defender) {
		return attacker instanceof Unit && ((Unit) attacker).hasTile() && !((Unit) attacker).getTile().isLand()
				&& defender instanceof Location && ((Location) defender).getTile() != null
				&& ((Location) defender).getTile().isLand();
	}

	/**
	 * Is this a combat between a rebel player and the REF at a colony?.
	 *
	 * @param attacker
	 *            the attacker
	 * @param defender
	 *            the defender
	 * @return True if the attack is a war of independence battle for a colony.
	 */
	public boolean combatIsWarOfIndependence(FreeColGameObject attacker, FreeColGameObject defender) {
		if (attacker instanceof Unit && defender instanceof Ownable) {
			Player aPlayer = ((Unit) attacker).getOwner();
			Player dPlayer = ((Ownable) defender).getOwner();
			return (aPlayer.isRebel() && aPlayer.getREFPlayer() == dPlayer)
					|| (dPlayer.isRebel() && dPlayer.getREFPlayer() == aPlayer);
		}
		return false;
	}

	/**
	 * Get a comparator to order units by relative military strength with
	 * respect to this combat model.
	 *
	 * @return A suitable unit <code>Comparator</code>.
	 */
	public final Comparator<Unit> getMilitaryStrengthComparator() {
		return (u1, u2) -> Double.compare(calculateCombatOdds(u1, u2).win, calculateCombatOdds(u2, u1).win);
	}

	/**
	 * Calculates the chance of the outcomes of a combat.
	 *
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return The <code>CombatOdds</code>.
	 */
	public abstract CombatOdds calculateCombatOdds(FreeColGameObject attacker, FreeColGameObject defender);

	/**
	 * Get the offensive power of a attacker wrt a defender.
	 *
	 * Null can be passed for the defender when only the attacker stats are
	 * required.
	 *
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return The offensive power.
	 */
	public abstract double getOffencePower(FreeColGameObject attacker, FreeColGameObject defender);

	/**
	 * Get the defensive power of a defender wrt an attacker.
	 *
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return The defensive power.
	 */
	public abstract double getDefencePower(FreeColGameObject attacker, FreeColGameObject defender);

	/**
	 * Collect all the offensive modifiers that apply to an attack.
	 *
	 * Null can be passed as the defender when only the attacker unit stats are
	 * required.
	 *
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return All the applicable offensive modifiers.
	 */
	public abstract Set<Modifier> getOffensiveModifiers(FreeColGameObject attacker, FreeColGameObject defender);

	/**
	 * Collect all defensive modifiers that apply to a unit defending against
	 * another.
	 *
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return All the applicable defensive modifiers.
	 */
	public abstract Set<Modifier> getDefensiveModifiers(FreeColGameObject attacker, FreeColGameObject defender);

	/**
	 * Generates a list of results of an attack. The first must be one of
	 * NO_RESULT, LOSE or WIN. The rest can be any other CombatResult suitable
	 * to the situation. To be called by the server only.
	 *
	 * @param random
	 *            A pseudo-random number source.
	 * @param attacker
	 *            The attacker.
	 * @param defender
	 *            The defender.
	 * @return The results of the combat.
	 */
	public abstract List<CombatResult> generateAttackResult(Random random, FreeColGameObject attacker,
			FreeColGameObject defender);
}
