/* This program is free software; you can redistribute it and/or modify
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
import l2jorion.game.model.Location;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.event.DM;
import l2jorion.game.model.entity.event.DMSpawnLocations;
import l2jorion.game.model.zone.L2ZoneType;
import l2jorion.game.model.zone.ZoneId;
import l2jorion.util.random.Rnd;

public class L2EventZone extends L2ZoneType
{
	public L2EventZone(final int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(ZoneId.ZONE_EVENT, true);
			
			if (((L2PcInstance) character).isGM())
			{
				((L2PcInstance) character).sendMessage("You enter: " + getName());
			}
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(ZoneId.ZONE_EVENT, false);
			
			L2PcInstance player = (L2PcInstance) character;
			
			if (player._inEventDM && DM.is_started())
			{
				if (Config.DM_SPAWN_LOCATIONS_FROM_LIST && DMSpawnLocations.getInstance().getAllSpawnLocations() != null)
				{
					final int offset = Config.DM_SPAWN_OFFSET;
					int x = 0, y = 0, z = 0;
					DM._playerSpawnLoc = (Location) (DMSpawnLocations.getInstance().getAllSpawnLocations()).toArray()[Rnd.get(0, DMSpawnLocations.getInstance().getAllSpawnLocations().size() - 1)];
					x = DM._playerSpawnLoc.getX() + Rnd.get(offset);
					y = DM._playerSpawnLoc.getY() + Rnd.get(offset);
					z = DM._playerSpawnLoc.getZ();
					player.teleToLocation(x, y, z);
				}
			}
			
			if (((L2PcInstance) character).isGM())
			{
				((L2PcInstance) character).sendMessage("You left: " + getName());
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