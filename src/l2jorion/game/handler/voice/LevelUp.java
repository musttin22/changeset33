/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2jorion.game.handler.voice;

import l2jorion.Config;
import l2jorion.game.datatables.xml.ExperienceData;
import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.model.L2Object;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.actor.instance.L2PlayableInstance;
import l2jorion.game.model.zone.ZoneId;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.SystemMessage;

public class LevelUp implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"setlvl"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
	{
		L2Object targetChar = player.getTarget();
		
		if (player.isInsideZone(ZoneId.ZONE_BOSS))
		{
			player.sendMessage("Command is not available in this area.");
			return false;
		}
		
		if (command.equalsIgnoreCase("setlvl"))
		{
			try
			{
				if (targetChar == null)
				{
					player.setTarget(player);
					targetChar = player.getTarget();
				}
				
				if (targetChar != player || !(targetChar instanceof L2PlayableInstance))
				{
					player.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET)); // incorrect
					player.sendMessage("Error! Set target yourself.");
					return false;
				}
				
				final L2PlayableInstance targetPlayer = (L2PlayableInstance) targetChar;
				final byte lvl = Byte.parseByte(target);
				int max_level = ExperienceData.getInstance().getMaxLevel();
				
				if (targetChar instanceof L2PcInstance && ((L2PcInstance) targetPlayer).isSubClassActive())
				{
					max_level = Config.MAX_SUBCLASS_LEVEL;
				}
				
				if (lvl >= 1 && lvl <= max_level)
				{
					final long pXp = targetPlayer.getStat().getExp();
					final long tXp = ExperienceData.getInstance().getExpForLevel(lvl);
					
					if (pXp > tXp)
					{
						targetPlayer.getStat().removeExpAndSp(pXp - tXp, 0);
					}
					else if (pXp < tXp)
					{
						targetPlayer.getStat().addExpAndSp(tXp - pXp, 0);
					}
				}
			}
			catch (final NumberFormatException e)
			{
				player.sendMessage("Error! You have to choose level, for example: .setlvl 81");
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
