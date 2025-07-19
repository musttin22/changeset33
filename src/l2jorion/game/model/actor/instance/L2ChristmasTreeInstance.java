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
package l2jorion.game.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import l2jorion.game.datatables.SkillTable;
import l2jorion.game.model.L2Skill;
import l2jorion.game.model.zone.ZoneId;
import l2jorion.game.network.serverpackets.ActionFailed;
import l2jorion.game.templates.L2NpcTemplate;
import l2jorion.game.thread.ThreadPoolManager;

public class L2ChristmasTreeInstance extends L2NpcInstance
{
	public static final int SPECIAL_TREE_ID = 13007;
	private ScheduledFuture<?> _aiTask;
	
	public L2ChristmasTreeInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		
		if (template.getNpcId() == SPECIAL_TREE_ID && !isInsideZone(ZoneId.ZONE_PEACE))
		{
			final L2Skill recoveryAura = SkillTable.getInstance().getInfo(2139, 1);
			if (recoveryAura == null)
			{
				return;
			}
			
			_aiTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
			{
				for (L2PcInstance player : getKnownList().getKnownTypeInRadius(L2PcInstance.class, 500))
				{
					if (player.getFirstEffect(recoveryAura) == null)
					{
						recoveryAura.getEffects(player, player);
					}
				}
			}, 3000, 3000);
		}
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
		}
		
		super.deleteMe();
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
