package org.powerbot.game.api.wrappers.interactive;

import java.lang.ref.SoftReference;

import org.powerbot.game.api.util.internal.Multipliers;
import org.powerbot.game.api.wrappers.Identifiable;
import org.powerbot.game.bot.Bot;
import org.powerbot.game.client.RSNPCDefActions;
import org.powerbot.game.client.RSNPCDefID;
import org.powerbot.game.client.RSNPCDefInts;
import org.powerbot.game.client.RSNPCDefName;
import org.powerbot.game.client.RSNPCInts;
import org.powerbot.game.client.RSNPCLevel;
import org.powerbot.game.client.RSNPCRSNPCDef;

/**
 * @author Timer
 */
public class Npc extends Character implements Identifiable {
	private final SoftReference<Object> n;
	private final Multipliers multipliers;

	public Npc(final Object n) {
		this.n = new SoftReference<Object>(n);
		this.multipliers = Bot.resolve().multipliers;
	}

	public int getLevel() {
		return ((RSNPCLevel) ((RSNPCInts) get()).getRSNPCInts()).getRSNPCLevel() * multipliers.NPC_LEVEL;
	}

	public String getName() {
		return (String) ((RSNPCDefName) ((RSNPCRSNPCDef) get()).getRSNPCRSNPCDef()).getRSNPCDefName();
	}

	public int getId() {
		return ((RSNPCDefID) ((RSNPCDefInts) ((RSNPCRSNPCDef) get()).getRSNPCRSNPCDef()).getRSNPCDefInts()).getRSNPCDefID() * multipliers.NPCDEF_ID;
	}

	public String[] getActions() {
		return (String[]) ((RSNPCDefActions) ((RSNPCRSNPCDef) get()).getRSNPCRSNPCDef()).getRSNPCDefActions();
	}

	public Object get() {
		return n.get();
	}
}
