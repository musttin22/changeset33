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
package l2jorion.game.model.actor.status;

import l2jorion.game.model.actor.instance.L2PetInstance;

public class PetStatus extends SummonStatus
{
	private int _currentFed = 0; // Current Fed of the L2PetInstance
	
	public PetStatus(final L2PetInstance activeChar)
	{
		super(activeChar);
	}
	
	public int getCurrentFed()
	{
		return _currentFed;
	}
	
	public void setCurrentFed(final int value)
	{
		_currentFed = value;
	}
}
