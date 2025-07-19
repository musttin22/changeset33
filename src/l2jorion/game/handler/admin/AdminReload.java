/*
 * L2jOrion Project - www.l2jorion.com 
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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package l2jorion.game.handler.admin;

import java.io.File;
import java.util.StringTokenizer;

import l2jorion.Config;
import l2jorion.game.cache.HtmCache;
import l2jorion.game.controllers.TradeController;
import l2jorion.game.datatables.GmListTable;
import l2jorion.game.datatables.SkillTable;
import l2jorion.game.datatables.csv.NpcWalkerRoutesTable;
import l2jorion.game.datatables.sql.AccessLevels;
import l2jorion.game.datatables.sql.AdminCommandAccessRights;
import l2jorion.game.datatables.sql.ClanTable;
import l2jorion.game.datatables.sql.HelperBuffTable;
import l2jorion.game.datatables.sql.ItemTable;
import l2jorion.game.datatables.sql.NpcTable;
import l2jorion.game.datatables.sql.TeleportLocationTable;
import l2jorion.game.datatables.xml.AugmentationData;
import l2jorion.game.datatables.xml.DressMeData;
import l2jorion.game.datatables.xml.ExperienceData;
import l2jorion.game.handler.IAdminCommandHandler;
import l2jorion.game.managers.AchievementManager;
import l2jorion.game.managers.AuctionManager;
import l2jorion.game.managers.ClassDamageManager;
import l2jorion.game.managers.DimensionalRiftManager;
import l2jorion.game.managers.QuestManager;
import l2jorion.game.managers.ZoneManager;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.Announcements;
import l2jorion.game.model.multisell.L2Multisell;
import l2jorion.game.powerpack.PowerPackConfig;
import l2jorion.game.powerpack.buffer.BuffsTable;
import l2jorion.game.script.faenor.FaenorScriptEngine;
import l2jorion.game.scripting.L2ScriptEngineManager;

public class AdminReload implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_reload"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_reload"))
		{
			sendReloadPage(activeChar);
			
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Usage:  //reload <type>");
				return false;
			}
			
			try
			{
				final String type = st.nextToken();
				
				if (type.equals("multisell"))
				{
					L2Multisell.getInstance().reload();
					activeChar.sendMessage("Multisell data has been reloaded.");
				}
				else if (type.startsWith("teleport"))
				{
					TeleportLocationTable.getInstance().reloadAll();
					activeChar.sendMessage("Teleport location table has been reloaded.");
				}
				else if (type.startsWith("skill"))
				{
					SkillTable.getInstance().reload();
					activeChar.sendMessage("Skills data has been reloaded.");
				}
				else if (type.equals("npc"))
				{
					NpcTable.getInstance().reloadAllNpc();
					activeChar.sendMessage("Npcs data has been reloaded.");
				}
				else if (type.startsWith("htm"))
				{
					activeChar.sendMessage("HTM / HTML data has been reloaded.");
					HtmCache.getInstance().reload();
					if (!Config.LAZY_CACHE)
					{
						activeChar.sendMessage("HTML Cache: " + HtmCache.getInstance().getMemoryUsage() + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded");
					}
				}
				else if (type.startsWith("item"))
				{
					ItemTable.getInstance().reload();
					activeChar.sendMessage("Item templates data has been reloaded.");
				}
				else if (type.startsWith("instancemanager"))
				{
					AuctionManager.getInstance().reload();
					
					if (!Config.ALT_DEV_NO_QUESTS)
					{
						QuestManager.getInstance();
						QuestManager.reload();
					}
					activeChar.sendMessage("All instance managers have been reloaded.");
				}
				else if (type.startsWith("npcwalkers"))
				{
					NpcWalkerRoutesTable.getInstance().load();
					activeChar.sendMessage("All NPC walker routes have been reloaded.");
				}
				else if (type.startsWith("quests"))
				{
					final String folder = "quests";
					QuestManager.getInstance().reload(folder);
					activeChar.sendMessage("Quests data has been reloaded.");
				}
				else if (type.startsWith("npcbuffers"))
				{
					BuffsTable.getInstance().realoadBuffsTable();
					activeChar.sendMessage("Buffer skills table has been reloaded.");
				}
				else if (type.equals("configs"))
				{
					Config.load();
					PowerPackConfig.load();
					activeChar.sendMessage("Server Configs data has been reloaded.");
				}
				else if (type.equals("tradelist"))
				{
					TradeController.reload();
					activeChar.sendMessage("TradeList table has been reloaded.");
				}
				else if (type.equals("dbs"))
				{
					AccessLevels.reload();
					activeChar.sendMessage("AccessLevels data has been reloaded.");
					AdminCommandAccessRights.reload();
					activeChar.sendMessage("AdminCommandAccessRights data has been reloaded.");
					GmListTable.reload();
					activeChar.sendMessage("GmListTable data has been reloaded.");
					AugmentationData.reload();
					activeChar.sendMessage("AugmentationData data has been reloaded.");
					ClanTable.reload();
					activeChar.sendMessage("ClanTable data has been reloaded.");
					HelperBuffTable.reload();
					activeChar.sendMessage("HelperBuffTable data has been reloaded.");
					ExperienceData.getInstance();
					activeChar.sendMessage("ExperienceData data has been reloaded.");
				}
				else if (type.startsWith("scripts_custom"))
				{
					try
					{
						final File custom_scripts_dir = new File(Config.DATAPACK_ROOT + "/data/scripts/custom");
						L2ScriptEngineManager.getInstance().executeAllScriptsInDirectory(custom_scripts_dir, true, 3);
					}
					catch (final Exception ioe)
					{
						activeChar.sendMessage("Failed loading " + Config.DATAPACK_ROOT + "/data/scripts/custom scripts, no script going to be loaded");
						ioe.printStackTrace();
					}
				}
				else if (type.startsWith("zone"))
				{
					ZoneManager.getInstance().reload();
					activeChar.sendMessage("Zones data has been reloaded.");
				}
				else if (type.startsWith("ach"))
				{
					AchievementManager.getInstance().reload();
					activeChar.sendMessage("Achievement data has been reloaded.");
				}
				else if (type.startsWith("classdamage"))
				{
					ClassDamageManager.reloadConfig();
					activeChar.sendMessage("Class damages data has been reloaded.");
				}
				else if (type.startsWith("scripts_faenor"))
				{
					try
					{
						Announcements.getInstance().getEventAnnouncements().clear();
						FaenorScriptEngine.getInstance().reloadFaenorScriptEngine();
						activeChar.sendMessage("Faenor data has been reloaded.");
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							activeChar.sendMessage("Failed loading faenor scripts, no script going to be loaded: " + e);
							e.printStackTrace();
						}
					}
				}
				else if (type.startsWith("skins"))
				{
					DressMeData.getInstance().reload();
					activeChar.sendMessage("Skins data has been reloaded.");
				}
				else if (type.startsWith("rift"))
				{
					DimensionalRiftManager.getInstance().reload();
					activeChar.sendMessage("Dimensional Rift data has been reloaded.");
				}
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Usage: //reload <type>");
			}
		}
		return true;
	}
	
	private void sendReloadPage(final L2PcInstance activeChar)
	{
		AdminHelpPage.showSubMenuPage(activeChar, "reload_menu.htm");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
