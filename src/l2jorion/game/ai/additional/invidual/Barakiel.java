package l2jorion.game.ai.additional.invidual;

import l2jorion.Config;
import l2jorion.game.model.L2Party;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.InventoryUpdate;
import l2jorion.game.network.serverpackets.MagicSkillUser;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.util.Util;

public class Barakiel extends Quest implements Runnable
{
	// Barakiel NpcID
	private static final int BARAKIEL = 25325;
	
	public Barakiel(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		addEventId(BARAKIEL, Quest.QuestEventType.ON_ATTACK);
		addEventId(BARAKIEL, Quest.QuestEventType.ON_KILL);
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		if (Config.L2LIMIT_CUSTOM || Config.ALPHA_CUSTOM || Config.CAROLMANIAC_CUSTOM || Config.DESIMKE_CUSTOM)
		{
			L2Party party = killer.getParty();
			
			if (party != null)
			{
				for (L2PcInstance member : party.getPartyMembers())
				{
					if (member == null || member.isNoble())
					{
						continue;
					}
					
					if (Config.DESIMKE_CUSTOM && !member.isSubClassActive())
					{
						continue;
					}
					
					if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, member, true))
					{
						continue;
					}
					
					member.setNoble(true);
					member.sendMessage("Congratulations! You've got The Nobless status");
					member.sendPacket(new ExShowScreenMessage("Congratulations! You've got The Nobless status", 3000, 0x02, false));
					PlaySound playSound = new PlaySound("ItemSound.quest_fanfare_1");
					member.sendPacket(playSound);
					member.broadcastUserInfo();
					
					if (Config.ALPHA_CUSTOM)
					{
						member.broadcastPacket(new MagicSkillUser(member, member, 1410, 1, 1000, 1));
					}
					
					L2ItemInstance newitem = member.getInventory().addItem("Tiara", 7694, 1, member, null);
					InventoryUpdate playerIU = new InventoryUpdate();
					playerIU.addItem(newitem);
					member.sendPacket(playerIU);
					SystemMessage sm;
					sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
					sm.addItemName(7694);
					member.sendPacket(sm);
				}
			}
			else
			{
				if (Config.DESIMKE_CUSTOM && !killer.isSubClassActive())
				{
					return super.onKill(npc, killer, isPet);
				}
				
				if (!killer.isNoble())
				{
					killer.setNoble(true);
					killer.sendMessage("Congratulations! You've got The Nobless status");
					killer.sendPacket(new ExShowScreenMessage("Congratulations! You've got The Nobless status", 3000, 0x02, false));
					PlaySound playSound = new PlaySound("ItemSound.quest_fanfare_1");
					killer.sendPacket(playSound);
					killer.broadcastUserInfo();
					
					if (Config.ALPHA_CUSTOM)
					{
						killer.broadcastPacket(new MagicSkillUser(killer, killer, 1410, 1, 1000, 1));
					}
					
					L2ItemInstance newitem = killer.getInventory().addItem("Tiara", 7694, 1, killer, null);
					InventoryUpdate playerIU = new InventoryUpdate();
					playerIU.addItem(newitem);
					killer.sendPacket(playerIU);
					SystemMessage sm;
					sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
					sm.addItemName(7694);
					killer.sendPacket(sm);
				}
			}
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public void run()
	{
	}
}
