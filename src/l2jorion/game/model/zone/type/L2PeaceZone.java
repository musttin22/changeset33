/* L2jOrion Project - www.l2jorion.com 
 * 
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
package l2jorion.game.model.zone.type;

import l2jorion.Config;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.actor.instance.L2MonsterInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.spawn.L2Spawn;
import l2jorion.game.model.zone.L2ZoneType;
import l2jorion.game.model.zone.ZoneId;

public class L2PeaceZone extends L2ZoneType
{
	private boolean _teleportCreatureOut = false;
	
	public L2PeaceZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("teleportCreatureOut"))
		{
			_teleportCreatureOut = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		character.setInsideZone(ZoneId.ZONE_PEACE, true);
		
		if (character instanceof L2MonsterInstance)
		{
			if (_teleportCreatureOut)
			{
				L2MonsterInstance creature = (L2MonsterInstance) character;
				L2Spawn mobSpawn = creature.getSpawn();
				if (mobSpawn != null)
				{
					creature.teleToLocation(mobSpawn.getLocx(), mobSpawn.getLocy(), mobSpawn.getLocz(), false);
				}
			}
		}
		
		if (character instanceof L2PcInstance)
		{
			if (Config.BOT_PROTECTOR)
			{
				L2PcInstance player = (L2PcInstance) character;
				player.stopBotChecker();
			}
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		character.setInsideZone(ZoneId.ZONE_PEACE, false);
		
		if (character instanceof L2PcInstance)
		{
			if (Config.BOT_PROTECTOR)
			{
				L2PcInstance player = (L2PcInstance) character;
				player.startBotChecker();
			}
		}
	}
	
	@Override
	public void onDieInside(final L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(final L2Character character)
	{
	}
	
}
