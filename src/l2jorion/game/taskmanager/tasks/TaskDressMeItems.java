/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2jorion.game.taskmanager.tasks;

import java.util.Map.Entry;

import l2jorion.game.datatables.sql.DressmeItems;
import l2jorion.game.model.L2World;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.templates.StatsSet;
import l2jorion.game.thread.ThreadPoolManager;

public final class TaskDressMeItems implements Runnable
{
	// private static final Logger LOG = LoggerFactory.getLogger(TaskDressMeItems.class);
	
	public TaskDressMeItems()
	{
		ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(this, 20000, 60000);
	}
	
	@Override
	public final void run()
	{
		if (DressmeItems.getInstance().getAllData() != null && DressmeItems.getInstance().getAllData().entrySet() != null)
		{
			for (Entry<String, StatsSet> uniqueKey : DressmeItems.getInstance().getAllData().entrySet())
			{
				String unique_id = uniqueKey.getValue().getString("unique_id");
				String skin_type = uniqueKey.getValue().getString("skin_type");
				int skin_id = uniqueKey.getValue().getInteger("skin_id");
				
				long timeLeft = (uniqueKey.getValue().getLong("sys_time") - System.currentTimeMillis());
				if (timeLeft <= 0)
				{
					L2PcInstance player = L2World.getInstance().getPlayer(Integer.parseInt(unique_id.substring(0, 9)));
					if (player != null && player.isOnline() == 1)
					{
						switch (skin_type.toLowerCase())
						{
							case "armor":
								if (player.getArmorSkinOption() == skin_id)
								{
									player.setArmorSkinOption(0);
								}
								player.deleteArmorSkin(skin_id);
								break;
							case "weapon":
								if (player.getWeaponSkinOption() == skin_id)
								{
									player.setWeaponSkinOption(0);
								}
								player.deleteWeaponSkin(skin_id);
								break;
							case "hair":
								if (player.getHairSkinOption() == skin_id)
								{
									player.setHairSkinOption(0);
								}
								player.deleteHairSkin(skin_id);
								break;
							case "face":
								if (player.getFaceSkinOption() == skin_id)
								{
									player.setFaceSkinOption(0);
								}
								player.deleteFaceSkin(skin_id);
								break;
							case "shield":
								if (player.getShieldSkinOption() == skin_id)
								{
									player.setShieldSkinOption(0);
								}
								player.deleteShieldSkin(skin_id);
								break;
						}
						
						DressmeItems.getInstance().deleteFromList(unique_id, true);
						player.broadcastUserInfo();
					}
				}
			}
		}
	}
	
	public static TaskDressMeItems getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected final static TaskDressMeItems _instance = new TaskDressMeItems();
	}
}