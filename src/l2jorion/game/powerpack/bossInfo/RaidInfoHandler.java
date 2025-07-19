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
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2jorion.game.powerpack.bossInfo;

import java.util.Collection;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2jorion.Config;
import l2jorion.game.cache.HtmCache;
import l2jorion.game.datatables.MobGroupTable;
import l2jorion.game.datatables.sql.NpcTable;
import l2jorion.game.handler.ICommunityBoardHandler;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.managers.GrandBossManager;
import l2jorion.game.managers.RaidBossSpawnManager;
import l2jorion.game.model.L2Attackable;
import l2jorion.game.model.actor.instance.L2ControllableMobInstance;
import l2jorion.game.model.actor.instance.L2GrandBossInstance;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.actor.instance.L2RaidBossInstance;
import l2jorion.game.model.zone.ZoneId;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.ActionFailed;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.game.network.serverpackets.ShowMiniMap;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.powerpack.PowerPackConfig;
import l2jorion.game.templates.L2NpcTemplate;
import l2jorion.game.templates.StatsSet;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.game.util.Util;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class RaidInfoHandler implements IVoicedCommandHandler, ICustomByPassHandler, ICommunityBoardHandler
{
	private static Logger LOG = LoggerFactory.getLogger(RaidInfoHandler.class);
	
	private String ROOT = "data/html/mods/boss/";
	public static int seconds = 1840;
	
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]
		{
			PowerPackConfig.RESPAWN_BOSS_COMMAND
		};
	}
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
	{
		if (player == null)
		{
			return false;
		}
		
		if (Config.L2LIMIT_CUSTOM)
		{
			if (player.getPremiumService() == 0 && !player.isInsideZone(ZoneId.ZONE_PEACE))
			{
				String msg = null;
				msg = "You can't use this command outside town.";
				player.sendMessage(msg);
				player.sendPacket(new ExShowScreenMessage(msg, 2000, 2, false));
				player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return false;
			}
		}
		
		if (PowerPackConfig.RESPAWN_BOSS_ONLY_FOR_LORD)
		{
			if (player.getClan() == null || player.getClan() != null && !player.getClan().hasCastle() || player.getClan() != null && !player.getClan().hasHideout())
			{
				player.sendMessage("This command is only for castle or clanhall owners.");
				return true;
			}
		}
		
		if (command.equalsIgnoreCase("boss"))
		{
			showHtm(player);
			return false;
		}
		
		return true;
	}
	
	private void showHtm(L2PcInstance player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(1);
		String text = HtmCache.getInstance().getHtm(ROOT + "index.htm");
		htm.setHtml(text);
		player.sendPacket(htm);
	}
	
	private void showRbListHtm(L2PcInstance player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(1);
		String text = HtmCache.getInstance().getHtm(ROOT + "rb_list.htm");
		htm.setHtml(text);
		
		if (player.getSelectedBoss().size() > 0)
		{
			htm.replace("%selected%", "<font color=3399ff>" + player.getSelectedBoss().get(0) + "</font> <a action=\"bypass -h custom_bosses_rb_list_unselect\">Unselect</a>");
		}
		else
		{
			htm.replace("%selected%", "-");
		}
		
		player.sendPacket(htm);
	}
	
	@Override
	public String[] getByPassCommands()
	{
		return new String[]
		{
			"bosses_gb_list",
			"bosses_rb_list",
			"bosses_rb_list_unselect",
			"bosses_rb_bylevels",
			"bosses_view_raid_boss",
			"bosses_view_raid_boss_info",
			"bosses_view_epic_boss",
			"bosses_rb_loc",
			"bosses_index"
		};
	}
	
	private enum CommandEnum
	{
		bosses_gb_list,
		bosses_rb_list,
		bosses_rb_list_unselect,
		bosses_rb_bylevels,
		bosses_view_raid_boss,
		bosses_view_raid_boss_info,
		bosses_view_epic_boss,
		bosses_rb_loc,
		bosses_index
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		StringTokenizer st = new StringTokenizer(command);
		StringTokenizer st2 = new StringTokenizer(parameters, " ");
		StringTokenizer st3 = new StringTokenizer(parameters, "_");
		
		CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
		{
			return;
		}
		
		if (Config.RON_CUSTOM)
		{
			if (!player.isInsideZone(ZoneId.ZONE_PEACE) && player.getPremiumService() != 5 && player.getLevel() > 40)
			{
				String msg = "You can't use AIO outside town.";
				player.sendMessage(msg);
				player.sendPacket(new ExShowScreenMessage(msg, 2000, 2, false));
				player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return;
			}
		}
		
		switch (comm)
		{
			case bosses_gb_list:
			{
				sendGrandBossesInfo(player);
				break;
			}
			case bosses_rb_list:
			{
				showRbListHtm(player);
				break;
			}
			case bosses_rb_list_unselect:
			{
				player.getSelectedBoss().clear();
				player.getRadar().removeAllMarkers();
				showRbListHtm(player);
				break;
			}
			case bosses_rb_bylevels:
			{
				String val = "";
				String val2 = "";
				String val3 = "";
				
				if (st2.hasMoreTokens())
				{
					val = st2.nextToken();
					val2 = st2.nextToken();
					val3 = st2.nextToken();
					
					try
					{
						final int minLv = Integer.parseInt(val);
						final int maxLv = Integer.parseInt(val2);
						final int page = Integer.parseInt(val3);
						
						sendRaidBossesInfo(player, minLv, maxLv, page);
						return;
					}
					catch (final NumberFormatException e)
					{
					}
				}
				break;
			}
			case bosses_view_epic_boss:
			{
				String x = "";
				String y = "";
				String z = "";
				
				if (st2.hasMoreTokens())
				{
					x = st2.nextToken();
					y = st2.nextToken();
					z = st2.nextToken();
					
					try
					{
						final int x1 = Integer.parseInt(x);
						final int y1 = Integer.parseInt(y);
						final int z1 = Integer.parseInt(z);
						
						doObserve(player, x1, y1, z1, 50);
						return;
					}
					catch (final NumberFormatException e)
					{
					}
				}
				break;
			}
			case bosses_view_raid_boss:
			{
				String x = "";
				String y = "";
				String z = "";
				
				if (st2.hasMoreTokens())
				{
					x = st2.nextToken();
					y = st2.nextToken();
					z = st2.nextToken();
					
					try
					{
						final int x1 = Integer.parseInt(x);
						final int y1 = Integer.parseInt(y);
						final int z1 = Integer.parseInt(z);
						
						doObserve(player, x1, y1, z1, 15);
						return;
					}
					catch (final NumberFormatException e)
					{
					}
				}
				break;
			}
			case bosses_view_raid_boss_info:
			{
				String npcId = "";
				
				if (st2.hasMoreTokens())
				{
					npcId = st2.nextToken();
					
					try
					{
						final int npcId1 = Integer.parseInt(npcId);
						
						npcInfo(player, npcId1);
						return;
					}
					catch (final NumberFormatException e)
					{
					}
				}
				break;
			}
			case bosses_rb_loc:
			{
				String x = "";
				String y = "";
				String z = "";
				String name = "";
				
				if (st2.hasMoreTokens())
				{
					x = st2.nextToken();
					y = st2.nextToken();
					z = st2.nextToken();
					name = st3.nextToken();
					
					int locx = Integer.parseInt(x);
					int locy = Integer.parseInt(y);
					int locz = Integer.parseInt(z);
					String locname = name;
					int substring = String.valueOf(locx).length() + String.valueOf(locy).length() + String.valueOf(locz).length() + 3;
					
					player.getRadar().removeAllMarkers();
					player.getRadar().addMarker(locx, locy, locz);
					
					player.sendPacket(new ShowMiniMap(1665));
					
					player.getSelectedBoss().add(locname.substring(substring));
					ThreadPoolManager.getInstance().scheduleGeneral(new loadMarkers(player), 500);
				}
				break;
			}
			case bosses_index:
			{
				showHtm(player);
				break;
			}
		}
	}
	
	private void sendGrandBossesInfo(L2PcInstance activeChar)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(5);
		htm.setFile(ROOT + "gb_list.htm");
		TextBuilder t = new TextBuilder();
		
		int color = 1;
		for (int boss : PowerPackConfig.RAID_INFO_IDS_LIST)
		{
			String name = "";
			String link;
			
			StatsSet info = GrandBossManager.getInstance().getStatsSet(boss);
			L2GrandBossInstance grand_boss = GrandBossManager.getInstance().getBoss(boss);
			int x = 0;
			int y = 0;
			int z = 0;
			
			L2NpcTemplate template = null;
			if ((template = NpcTable.getInstance().getTemplate(boss)) != null)
			{
				name = template.getName();
			}
			else
			{
				LOG.error("Grand Boss with ID " + boss + " is not defined into NpcTable");
				continue;
			}
			
			long delay = info.getLong("respawn_time");
			String deadTime = info.getString("killed_time");
			
			if (grand_boss != null && grand_boss.isChampion())
			{
				name = "<font color=\"ff0000\">" + name + "</font>";
			}
			
			name = name + "&nbsp;<font color=\"ffff00\">" + template.getLevel() + "</font>";
			
			if (Config.RON_CUSTOM)
			{
				link = "<a action=\"bypass custom_bosses_view_raid_boss_info " + boss + "\">" + name + "</a>";
			}
			else
			{
				link = "" + name + "";
			}
			
			if (grand_boss != null && grand_boss.getSpawn() != null)
			{
				x = grand_boss.getSpawn().getLocx();
				y = grand_boss.getSpawn().getLocy();
				z = grand_boss.getSpawn().getLocz();
			}
			else
			{
				x = info.getInteger("loc_x");
				y = info.getInteger("loc_y");
				z = info.getInteger("loc_z");
			}
			
			if (color == 1)
			{
				color = 2;
				t.append("<table width=300 border=0 bgcolor=000000><tr>");
			}
			else
			{
				color = 1;
				t.append("<table width=300 border=0><tr>");
			}
			t.append("<td width=150>" + link + "</td><td width=110>" + (Config.RON_CUSTOM ? "" : (delay <= System.currentTimeMillis() ? //
				"<font color=\"009900\">Alive</font>" : "<font color=\"FF0000\">Dead: " + deadTime + "</font>") + "</td>") + //
				"" + (Config.RON_CUSTOM ? "<td width=40>[<a action=\"bypass custom_bosses_view_epic_boss " + x + " " + y + " " + z + "\">View</a>] <font color=LEVEL>50</font> PCB Points</td>" : "") + "");
			t.append("</tr></table>");
		}
		htm.replace("%bosses%", t.toString());
		activeChar.sendPacket(htm);
	}
	
	private void sendRaidBossesInfo(L2PcInstance activeChar, int minLv, int maxLv, int page)
	{
		final Collection<L2RaidBossInstance> rBosses = RaidBossSpawnManager._bossesForCommand.values();
		
		RaidBossSpawnManager.BOSSES_LIST.clear();
		
		for (final L2RaidBossInstance actual_boss : rBosses)
		{
			if ((actual_boss.getLevel() >= minLv && actual_boss.getLevel() <= maxLv))
			{
				RaidBossSpawnManager.BOSSES_LIST.add(actual_boss);
			}
		}
		
		RaidBossSpawnManager.BOSSES_LIST.sort((o1, o2) -> String.valueOf(o1.getLevel()).compareTo(String.valueOf(o2.getLevel())));
		
		L2RaidBossInstance[] bossses = RaidBossSpawnManager.BOSSES_LIST.toArray(new L2RaidBossInstance[RaidBossSpawnManager.BOSSES_LIST.size()]);
		
		final int MaxCharactersPerPage = 15;
		int MaxPages = bossses.length / MaxCharactersPerPage;
		
		if (bossses.length > MaxCharactersPerPage * MaxPages)
		{
			MaxPages++;
		}
		
		if (page > MaxPages)
		{
			page = MaxPages;
		}
		
		final int CharactersStart = MaxCharactersPerPage * page;
		int CharactersEnd = bossses.length;
		
		if (CharactersEnd - CharactersStart > MaxCharactersPerPage)
		{
			CharactersEnd = CharactersStart + MaxCharactersPerPage;
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(6);
		html.setFile(ROOT + "rb_list_bylevels.htm");
		TextBuilder th = new TextBuilder();
		
		int count = CharactersStart;
		int color = 1;
		for (int i = CharactersStart; i < CharactersEnd; i++)
		{
			count++;
			
			String name = "";
			int rbossId = bossses[i].getNpcId();
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(rbossId);
			StatsSet rInfo = RaidBossSpawnManager.getInstance().getStatsSet(rbossId);
			L2RaidBossInstance raid_boss = RaidBossSpawnManager.getInstance().getBoss(rbossId);
			long delay = 0;
			String deadTime = "";
			String link;
			int x = 0;
			int y = 0;
			int z = 0;
			
			if (rInfo != null)
			{
				name = template.getName();
				
				String locname = template.getName();
				
				if (name.length() >= 16)
				{
					name = name.substring(0, 16) + "...";
				}
				
				if (raid_boss != null && raid_boss.isChampion())
				{
					name = "<font color=\"ff0000\">" + name + "</font>";
				}
				
				name = name + "&nbsp;<font color=\"ffff00\">" + template.getLevel() + "</font>";
				
				if (template.aggroRange > 0)
				{
					name = name + "<font color=\"ff0000\">*</font>";
				}
				
				delay = rInfo.getLong("respawn_time");
				deadTime = rInfo.getString("killed_time");
				
				if (raid_boss != null && raid_boss.getSpawn() != null)
				{
					x = raid_boss.getSpawn().getLocx();
					y = raid_boss.getSpawn().getLocy();
					z = raid_boss.getSpawn().getLocz();
				}
				else
				{
					x = rInfo.getInteger("loc_x");
					y = rInfo.getInteger("loc_y");
					z = rInfo.getInteger("loc_z");
				}
				
				if (Config.RON_CUSTOM)
				{
					link = "<a action=\"bypass custom_bosses_view_raid_boss_info " + rbossId + "\">" + name + "</a>";
				}
				else
				{
					link = "<a action=\"bypass custom_bosses_rb_loc " + x + " " + y + " " + z + " " + locname + "\">" + name + "</a>";
				}
				
				if (color == 1)
				{
					color = 2;
					th.append("<table width=300 border=0 bgcolor=000000><tr>");
					
				}
				else
				{
					color = 1;
					th.append("<table width=300 border=0><tr>");
				}
				
				th.append("<td width=20>" + count + ".</td><td width=130>" + link + "</td>" + (Config.RON_CUSTOM ? "" : "<td width=110>" + (delay <= System.currentTimeMillis() ? "<font color=\"009900\">Alive</font>" : "<font color=\"FF0000\">Dead: " + deadTime + "</font>") + "</td>") //
					+ (Config.RON_CUSTOM ? "<td width=120>[<a action=\"bypass custom_bosses_view_raid_boss " + x + " " + y + " " + z + "\">View</a>] [<a action=\"bypass custom_bosses_rb_loc " + x + " " + y + " " + z + " " + locname + "\">Loc</a>] <font color=LEVEL>15</font> PCB Points</td>" : "")
					+ "");
				th.append("</tr></table>");
				
			}
		}
		
		html.replace("%raidbosses%", th.toString());
		
		th.clear();
		
		th.append("<center><table><tr>");
		for (int x = 0; x < MaxPages; x++)
		{
			final int pagenr = x + 1;
			if (page == x)
			{
				th.append("<td width=20>[" + pagenr + "]&nbsp;&nbsp;</td>");
			}
			else
			{
				th.append("<td width=20><a action=\"bypass -h custom_bosses_rb_bylevels " + minLv + " " + maxLv + " " + x + "\">[" + pagenr + "]</a>&nbsp;&nbsp;</td>");
			}
		}
		th.append("</tr></table></center>");
		
		html.replace("%pages%", th.toString());
		
		activeChar.sendPacket(html);
	}
	
	public static class loadMarkers implements Runnable
	{
		private final L2PcInstance _me;
		
		public loadMarkers(L2PcInstance me)
		{
			_me = me;
		}
		
		@Override
		public void run()
		{
			try
			{
				_me.getRadar().loadMarkers();
				_me.sendPacket(new CreatureSay(0, 17, "Your selected The Boss", _me.getSelectedBoss().get(0)));
			}
			catch (final Throwable t)
			{
			}
		}
	}
	
	private void doObserve(L2PcInstance player, int x, int y, int z, int price)
	{
		if (player.getPcBangScore() < price)
		{
			player.sendMessage("You don't have enough PC Bang Points. Required:" + price);
			player.sendPacket(new ExShowScreenMessage("You don't have enough PC Bang Points. Required:" + price, 1000, 2, false));
			player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
			return;
		}
		
		player.reducePcBangScore(price);
		player.sendPacket(new SystemMessage(SystemMessageId.USING_S1_PCPOINT).addNumber(price));
		
		player.enterObserverMode(x, y, z);
		player.setBossTaskNull();
		if (player.getBossTask() == null)
		{
			seconds = 1840;
			player.setBossObserve(true);
			player.sendMessage("Time left: 30 minutes");
			player.sendPacket(new ExShowScreenMessage("Time left: 30 minutes", 5000, 3, false));
			player.startBossTask();
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void bossObserveTimer(L2PcInstance player)
	{
		if (seconds > 0)
		{
			seconds--;
		}
		
		switch (seconds)
		{
			case 1900:
			case 1840:
			case 1780:
			case 1720:
			case 1660:
			case 1600:
			case 1540:
			case 1480:
			case 1420:
			case 1380:
			case 1320:
			case 1260:
			case 1200:
			case 1140:
			case 1080:
			case 1020:
			case 960:
			case 900:
			case 840:
			case 780:
			case 720:
			case 660:
			case 600:
			case 540:
			case 480:
			case 420:
			case 360:
			case 300:
			case 240:
			case 180:
			case 120:
				break;
			case 60:
				break;
			case 30:
			case 15:
			case 10:
			case 3:
			case 2:
				player.sendMessage("Time left: " + (seconds) + " seconds");
				player.sendPacket(new ExShowScreenMessage("Time left: " + (seconds) + " seconds", 5000, 3, false));
				break;
			case 1:
				player.sendMessage("Time left: " + (seconds) + " seconds");
				player.sendPacket(new ExShowScreenMessage("Time left: " + (seconds) + " seconds", 5000, 3, false));
				break;
			case 0:
				player.leaveObserverMode();
				player.setBossObserve(false);
				player.setBossTaskNull();
				player.sendMessage("Teleporting back");
				player.sendPacket(new ExShowScreenMessage("Teleporting back", 2000, 2, false));
				break;
		}
	}
	
	public void npcInfo(L2PcInstance player, int npcId)
	{
		if (player == null)
		{
			return;
		}
		
		L2NpcInstance npc = RaidBossSpawnManager.getInstance().getBoss(npcId);
		if (npc == null)
		{
			npc = GrandBossManager.getInstance().getBoss(npcId);
		}
		
		if (player.getAccessLevel().isGm() || (Config.ALT_GAME_VIEWNPC))
		{
			player.setTarget(npc);
			
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/npcinfo2.htm");
			
			html.replace("%hpbar%", Util.generateBar(150, 12, (long) npc.getCurrentHp(), npc.getMaxHp(), "HP"));
			html.replace("%mpbar%", Util.generateBar(150, 12, (long) npc.getCurrentMp(), npc.getMaxMp(), "MP"));
			html.replace("%title%", String.valueOf(npc.getTitle()));
			
			html.replace("%objid%", String.valueOf(npc.getObjectId()));
			html.replace("%type%", String.valueOf(npc.getTemplate().getType()).substring(2));
			html.replace("%race%", npc.getTemplate().getRace().toString().substring(0, 1).toUpperCase() + npc.getTemplate().getRace().toString().toLowerCase().substring(1));
			html.replace("%id%", String.valueOf(npc.getTemplate().getNpcId()));
			html.replace("%lvl%", String.valueOf(npc.getTemplate().getLevel()));
			html.replace("%name%", String.valueOf((npc.getTemplate().getName().length() >= 25 ? npc.getTemplate().getName().substring(0, 25) : npc.getTemplate().getName())));
			html.replace("%tmplid%", String.valueOf(npc.getTemplate().getTemplateId()));
			html.replace("%aggro%", String.valueOf((npc instanceof L2Attackable) ? npc.getAggroRange() : 0));
			html.replace("%hp%", String.valueOf((int) npc.getCurrentHp()));
			html.replace("%hpmax%", String.valueOf(npc.getMaxHp()));
			html.replace("%mp%", String.valueOf((int) npc.getCurrentMp()));
			html.replace("%mpmax%", String.valueOf(npc.getMaxMp()));
			
			if (npc instanceof L2ControllableMobInstance)
			{
				html.replace("%mobGroup%", String.valueOf(MobGroupTable.getInstance().getGroupForMob((L2ControllableMobInstance) npc).getGroupId()));
			}
			else
			{
				html.replace("%mobGroup%", String.valueOf("-"));
			}
			
			html.replace("%respawnTime%", String.valueOf((npc.getSpawn() != null ? npc.getSpawn().getRespawnDelay() / 1000 + "  Seconds" : "-")));
			
			html.replace("%fId%", String.valueOf(npc.getTemplate().getFactionId().isEmpty() ? "-" : npc.getTemplate().getFactionId()));
			html.replace("%fRange%", String.valueOf(npc.getTemplate().getFactionRange()));
			html.replace("%locId%", String.valueOf((npc.getSpawn() != null ? npc.getSpawn().getLocation() : 0)));
			html.replace("%spawnId%", String.valueOf((npc.getSpawn() != null ? npc.getSpawn().getId() : 0)));
			html.replace("%patk%", String.valueOf(npc.getPAtk(null)));
			html.replace("%matk%", String.valueOf(npc.getMAtk(null, null)));
			html.replace("%pdef%", String.valueOf(npc.getPDef(null)));
			html.replace("%mdef%", String.valueOf(npc.getMDef(null, null)));
			html.replace("%accu%", String.valueOf(npc.getAccuracy()));
			html.replace("%evas%", String.valueOf(npc.getEvasionRate(null)));
			html.replace("%crit%", String.valueOf(npc.getCriticalHit(null, null)));
			html.replace("%rspd%", String.valueOf(npc.getRunSpeed()));
			html.replace("%aspd%", String.valueOf(npc.getPAtkSpd()));
			html.replace("%cspd%", String.valueOf(npc.getMAtkSpd()));
			html.replace("%str%", String.valueOf(npc.getSTR()));
			html.replace("%dex%", String.valueOf(npc.getDEX()));
			html.replace("%con%", String.valueOf(npc.getCON()));
			html.replace("%int%", String.valueOf(npc.getINT()));
			html.replace("%wit%", String.valueOf(npc.getWIT()));
			html.replace("%men%", String.valueOf(npc.getMEN()));
			html.replace("%loc%", String.valueOf(npc.getX() + " " + npc.getY() + " " + npc.getZ()));
			html.replace("%heading%", String.valueOf(npc.getHeading()));
			html.replace("%collision_radius%", String.valueOf(npc.getTemplate().getCollisionRadius()));
			html.replace("%collision_height%", String.valueOf(npc.getTemplate().getCollisionHeight()));
			html.replace("%undead%", npc.isUndead() ? "<font color=00FF00>Yes</font>" : "<font color=FF0000>No</font>");
			player.sendPacket(html);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public static RaidInfoHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidInfoHandler INSTANCE = new RaidInfoHandler();
	}
	
	@Override
	public String[] getBypassBbsCommands()
	{
		return new String[]
		{
			"bbsboss"
		};
	}
}
