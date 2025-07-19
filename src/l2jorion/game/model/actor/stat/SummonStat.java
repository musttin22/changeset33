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
package l2jorion.game.model.actor.stat;

import l2jorion.game.model.L2Summon;
import l2jorion.game.skills.Stats;

public class SummonStat extends PlayableStat
{
	private int _oldMaxHp; // stats watch
	private int _oldMaxMp; // stats watch
	
	public SummonStat(final L2Summon activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public int getMaxHp()
	{
		int val = (int) calcStat(Stats.MAX_HP, getActiveChar().getTemplate().baseHpMax, null, null);
		
		if (val != _oldMaxHp)
		{
			_oldMaxHp = val;
			
			final L2Summon player = getActiveChar();
			
			// Launch a regen task if the new Max HP is higher than the old one
			if (player.getStatus().getCurrentHp() != val)
			{
				player.getStatus().setCurrentHp(player.getStatus().getCurrentHp()); // trigger start of regeneration
			}
		}
		
		return val;
	}
	
	@Override
	public int getMaxMp()
	{
		int val = (int) calcStat(Stats.MAX_MP, getActiveChar().getTemplate().baseMpMax, null, null);
		
		if (val != _oldMaxMp)
		{
			_oldMaxMp = val;
			
			final L2Summon player = getActiveChar();
			
			// Launch a regen task if the new Max MP is higher than the old one
			if (player.getStatus().getCurrentMp() != val)
			{
				player.getStatus().setCurrentMp(player.getStatus().getCurrentMp()); // trigger start of regeneration
			}
		}
		
		return val;
	}
	
	@Override
	public L2Summon getActiveChar()
	{
		return (L2Summon) super.getActiveChar();
	}
}
