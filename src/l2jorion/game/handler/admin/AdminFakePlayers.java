package l2jorion.game.handler.admin;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javolution.text.TextBuilder;
import l2jorion.Config;
import l2jorion.bots.FakePlayer;
import l2jorion.bots.FakePlayerManager;
import l2jorion.bots.FakePlayerTaskManager;
import l2jorion.bots.ai.StanderAI;
import l2jorion.bots.ai.WalkerAI;
import l2jorion.bots.model.FarmLocation;
import l2jorion.bots.model.WalkNode;
import l2jorion.bots.xml.botFarm;
import l2jorion.bots.xml.botRandomWalk;
import l2jorion.game.handler.IAdminCommandHandler;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2Object;
import l2jorion.game.model.L2World;
import l2jorion.game.model.Location;
import l2jorion.game.model.actor.instance.L2MonsterInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.taskmanager.RandomZoneTaskManager;
import l2jorion.util.random.Rnd;

public class AdminFakePlayers implements IAdminCommandHandler
{
	private final String botsFolder = "data/html/admin/bots/";
	
	private boolean showByLvl = false;
	private boolean showByName = false;
	
	private boolean showNewbieBots = false;
	private boolean showPvPBots = false;
	private boolean showTownBots = false;
	private boolean showFarmBots = false;
	
	protected WalkNode _currentWalkNode;
	protected FarmLocation _currentFarmLoc;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_bots",
		"admin_spawnbot",
		"admin_spawnrandom",
		"admin_deletebot",
		"admin_spawnenchanter",
		"admin_spawnwalker",
		"admin_deleteallbots",
		"admin_killallbots",
		"admin_ressallbots",
		"admin_takecontrol",
		"admin_releasecontrol",
		"admin_bot_info",
		"admin_show_bots_opion"
	};
	
	private enum CommandEnum
	{
		admin_bots,
		admin_spawnbot,
		admin_spawnrandom,
		admin_deletebot,
		admin_spawnenchanter,
		admin_spawnwalker,
		admin_deleteallbots,
		admin_killallbots,
		admin_ressallbots,
		admin_takecontrol,
		admin_releasecontrol,
		admin_bot_info,
		admin_show_bots_opion
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.BOTS_SYSTEM)
		{
			activeChar.sendMessage("You can't use this feature at the moment. Bots system is disabled.");
			return false;
		}
		
		StringTokenizer st = new StringTokenizer(command);
		
		CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_bots:
			{
				String val = "";
				
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
					
					try
					{
						final int page = Integer.parseInt(val);
						listBots(activeChar, page);
						return true;
					}
					catch (final NumberFormatException e)
					{
						listBots(activeChar, 0);
						return false;
					}
				}
				listBots(activeChar, 0);
				return false;
			}
			case admin_spawnbot:
			{
				// Class<? extends L2Character> targetClass = null;
				
				int bots = 1;
				String type = null;
				// int range = 2000;
				// int radius = 0;
				// String target = null;
				
				if (st.countTokens() > 1)
				{
					bots = Integer.parseInt(st.nextToken());
					type = st.nextToken();
				}
				else
				{
					bots = 1;
					type = st.nextToken();
				}
				
				// range = Integer.parseInt(st.nextToken());
				// target = st.nextToken();
				// radius = Integer.parseInt(st.nextToken());
				
				/*
				 * switch (target.toUpperCase()) { case "PLAYER": targetClass = L2PcInstance.class; break; case "MONSTE": targetClass = L2MonsterInstance.class; break; case "BOTH": targetClass = L2Character.class; break; }
				 */
				
				switch (type)
				{
					case "NEWBIE":
						for (int i = 0; i < bots; i++)
						{
							// final int x = (int) (radius * Math.cos(i * 1.777));
							// final int y = (int) (radius * Math.sin(i * 1.777));
							// FakePlayer fakePlayer = FakePlayerManager.INSTANCE.spawnPlayer(activeChar.getX() + x, activeChar.getY() + y, activeChar.getZ(), 1, 0, false, false, false, true);
							
							FakePlayer fakePlayer = FakePlayerManager.INSTANCE.spawnPlayer(0, 0, 0, 1, 0, false, false, false, true);
							fakePlayer.setBotMode(1);
							fakePlayer.setTargetClass(L2MonsterInstance.class);
							fakePlayer.setTargetRange(300);
							fakePlayer.assignDefaultAI();
						}
						break;
					case "TOWN":
						int x = 0, y = 0;
						for (int i = 0; i < bots; i++)
						{
							int rndLevel = 81;
							int rndClassId = 3;
							
							if (Config.BOTS_CHANCE_FOR_NEWBIE_WALK > Rnd.get(100))
							{
								rndLevel = 1;
								rndClassId = 0;
							}
							
							int townId = Rnd.get(1, botRandomWalk.getInstance().getLastTownId());
							
							_currentWalkNode = (WalkNode) botRandomWalk.getInstance().getWalkNode(townId).toArray()[Rnd.get(0, botRandomWalk.getInstance().getWalkNode(townId).size() - 1)];
							
							x = _currentWalkNode.getX();
							y = _currentWalkNode.getY();
							
							x += Rnd.get(-Config.BOTS_RANDOM_MAX_OFFSET, Config.BOTS_RANDOM_MAX_OFFSET);
							y += Rnd.get(-Config.BOTS_RANDOM_MAX_OFFSET, Config.BOTS_RANDOM_MAX_OFFSET);
							
							FakePlayer fakePlayer = FakePlayerManager.INSTANCE.spawnPlayer(x, y, _currentWalkNode.getZ(), rndLevel, rndClassId, false, true, false, false);
							fakePlayer.setBotMode(2);
							
							if (Config.BOTS_CHANCE_FOR_WALK > Rnd.get(100))
							{
								fakePlayer.setTownId(townId);
								fakePlayer.setFakeAi(new WalkerAI(fakePlayer));
							}
							else
							{
								fakePlayer.setFakeAi(new StanderAI(fakePlayer));
							}
						}
						break;
					case "PVP":
						
						if (!Config.ALLOW_RANDOM_PVP_ZONE)
						{
							activeChar.sendMessage("You can't use this feature at the moment. Random PvP Zone feature is disabled.");
							return false;
						}
						
						for (int i = 0; i < bots; i++)
						{
							Location location = RandomZoneTaskManager.getInstance().getCurrentZone().getLoc();
							FakePlayer fakePlayer = FakePlayerManager.INSTANCE.spawnPlayer(location.getX(), location.getY(), location.getZ(), 81, 3, true, false, false, false);
							fakePlayer.setBotMode(3);
							fakePlayer.setTargetClass(L2PcInstance.class);
							fakePlayer.setTargetRange(Config.BOTS_PVP_ZONE_ATTACK_RANGE);
							fakePlayer.assignDefaultAI();
							
						}
						break;
					case "FARM":
						for (int i = 0; i < bots; i++)
						{
							int zoneId = Rnd.get(1, botFarm.getInstance().getLastZoneId());
							int rndOffsetX = Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
							int rndOffsetY = Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
							_currentFarmLoc = (FarmLocation) botFarm.getInstance().getFarmNode(zoneId).toArray()[Rnd.get(0, botFarm.getInstance().getFarmNode(zoneId).size() - 1)];
							FakePlayer fakePlayer = FakePlayerManager.INSTANCE.spawnPlayer(_currentFarmLoc.getX() + rndOffsetX, _currentFarmLoc.getY() + rndOffsetY, _currentFarmLoc.getZ(), 81, 3, false, false, true, false);
							fakePlayer.setDistance(_currentFarmLoc);
							
							if (_currentFarmLoc.getType().equals("peace"))
							{
								fakePlayer.setBotMode(5);
							}
							else
							{
								fakePlayer.setBotMode(4);
							}
							
							fakePlayer.setZoneId(zoneId);
							fakePlayer.setTargetClass(L2Character.class);
							fakePlayer.setTargetRange(300);
							fakePlayer.setMaxTargetRange(3500);
							fakePlayer.assignDefaultAI();
						}
						break;
				}
				
				listBots(activeChar, 0);
				break;
			}
			case admin_deletebot:
			{
				String target = null;
				target = st.nextToken();
				
				L2PcInstance player = null;
				
				if (activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance)
				{
					player = (L2PcInstance) activeChar.getTarget();
				}
				
				if (activeChar.getTarget() == null)
				{
					player = L2World.getInstance().getPlayer(target);
				}
				
				if (player != null && player instanceof FakePlayer)
				{
					FakePlayer fakePlayer = (FakePlayer) player;
					fakePlayer.despawnPlayer();
				}
				
				listBots(activeChar, 0);
				break;
			}
			case admin_takecontrol:
			{
				if (activeChar.getTarget() != null && activeChar.getTarget() instanceof FakePlayer)
				{
					FakePlayer fakePlayer = (FakePlayer) activeChar.getTarget();
					fakePlayer.setUnderControl(true);
					activeChar.setPlayerUnderControl(fakePlayer);
					activeChar.sendMessage("You are now controlling: " + fakePlayer.getName());
					return true;
				}
				activeChar.sendMessage("You can only take control of a Bot Player");
				break;
			}
			case admin_releasecontrol:
			{
				if (activeChar.isControllingFakePlayer())
				{
					FakePlayer fakePlayer = activeChar.getPlayerUnderControl();
					activeChar.sendMessage("You are no longer controlling: " + fakePlayer.getName());
					fakePlayer.setUnderControl(false);
					activeChar.setPlayerUnderControl(null);
					return true;
				}
				activeChar.sendMessage("You are not controlling a Bot Player");
				break;
			}
			case admin_deleteallbots:
			{
				int number = L2World.getInstance().getPlayers().stream().filter(player -> player instanceof FakePlayer).collect(Collectors.toList()).size();
				L2World.getInstance().getPlayers().stream().filter(player -> player instanceof FakePlayer).forEach(player -> ((FakePlayer) player).despawnPlayer());
				activeChar.sendMessage("Kicked: " + number);
				break;
			}
			case admin_killallbots:
			{
				int number = L2World.getInstance().getPlayers().stream().filter(player -> player instanceof FakePlayer).collect(Collectors.toList()).size();
				L2World.getInstance().getPlayers().stream().filter(player -> player instanceof FakePlayer).forEach(player -> player.reduceCurrentHp(player.getMaxHp() + player.getMaxCp() + 1, activeChar));
				activeChar.sendMessage("Killed: " + number);
				break;
			}
			case admin_ressallbots:
			{
				int number = L2World.getInstance().getPlayers().stream().filter(player -> player instanceof FakePlayer).collect(Collectors.toList()).size();
				L2World.getInstance().getPlayers().stream().filter(player -> player instanceof FakePlayer).forEach(player -> player.doRevive());
				activeChar.sendMessage("Resurrected: " + number);
				break;
			}
			case admin_bot_info:
			{
				String val = "";
				
				if (st.hasMoreTokens())
				{
					while (st.hasMoreTokens())
					{
						if (val.equals(""))
						{
							val = st.nextToken();
						}
						else
						{
							val += " " + st.nextToken();
						}
					}
				}
				else
				{
					activeChar.sendMessage("Usage: //character_info <player_name>");
					return false;
				}
				
				final L2PcInstance target = L2World.getInstance().getPlayer(val);
				
				if (target != null)
				{
					showBotInfo(activeChar, target);
					return true;
				}
				
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
				return false;
			}
			case admin_show_bots_opion:
			{
				String option = "";
				
				if (st.hasMoreTokens())
				{
					option = st.nextToken();
					
					switch (Integer.parseInt(option))
					{
						case 0: // by online time
						{
							setShowByLvl(false);
							setShowByName(false);
							break;
						}
						case 1: // by level
						{
							if (isShowByLvl())
							{
								setShowByLvl(false);
							}
							else
							{
								setShowByName(false);
								setShowByLvl(true);
							}
							break;
						}
						case 2: // by name
						{
							if (isShowByName())
							{
								setShowByName(false);
							}
							else
							{
								setShowByLvl(false);
								setShowByName(true);
							}
							break;
						}
						// XXX Bots options
						case 3: // show newbie bots
						{
							if (isShowingNewbieBots())
							{
								setNewbieBots(false);
							}
							else
							{
								setNewbieBots(true);
								setPvPBots(false);
								setTownBots(false);
								setFarmBots(false);
							}
							break;
						}
						case 4: // show pvp bots
						{
							if (isShowingPvPBots())
							{
								setPvPBots(false);
							}
							else
							{
								setNewbieBots(false);
								setPvPBots(true);
								setTownBots(false);
								setFarmBots(false);
							}
							break;
						}
						case 5: // show town bots
						{
							if (isShowingTownBots())
							{
								setTownBots(false);
							}
							else
							{
								setNewbieBots(false);
								setPvPBots(false);
								setTownBots(true);
								setFarmBots(false);
							}
							break;
						}
						case 6: // show farm bots
						{
							if (isShowingFarmBots())
							{
								setFarmBots(false);
							}
							else
							{
								setNewbieBots(false);
								setPvPBots(false);
								setTownBots(false);
								setFarmBots(true);
							}
							break;
						}
					}
					
					try
					{
						listBots(activeChar, 0);
						return true;
					}
					catch (final NumberFormatException e)
					{
						return false;
					}
				}
				return false;
			}
			default:
				break;
		}
		
		return true;
	}
	
	private void listBots(final L2PcInstance activeChar, int page)
	{
		// Page limit
		int pageLimit = 10;
		// List
		Collection<L2PcInstance> onlineList = L2World.getInstance().getAllPlayers().values();
		List<L2PcInstance> list = null;
		
		if (isShowByLvl())
		{
			list = onlineList.stream().sorted(Comparator.comparingLong(player -> ((L2PcInstance) player).getExp()).reversed()).filter(player -> player != null && player.isBot() && //
				((isShowingNewbieBots() ? ((FakePlayer) player).getBotMode() == 1 : //
					(isShowingPvPBots() ? ((FakePlayer) player).getBotMode() == 3 : //
						(isShowingTownBots() ? ((FakePlayer) player).getBotMode() == 2 : //
							(isShowingFarmBots() ? ((FakePlayer) player).getBotMode() == 4 : player.isBot()//
							)))))).collect(Collectors.toList());
		}
		else if (isShowByName())
		{
			list = onlineList.stream().sorted(Comparator.comparing(player -> player.getName())).filter(player -> player != null && player.isBot() && //
				((isShowingNewbieBots() ? ((FakePlayer) player).getBotMode() == 1 : //
					(isShowingPvPBots() ? ((FakePlayer) player).getBotMode() == 3 : //
						(isShowingTownBots() ? ((FakePlayer) player).getBotMode() == 2 : //
							(isShowingFarmBots() ? ((FakePlayer) player).getBotMode() == 4 : player.isBot()//
							)))))).collect(Collectors.toList());
		}
		else
		{
			list = onlineList.stream().sorted(Comparator.comparingLong(player -> player.getOnlineTime())).filter(player -> player != null && player.isBot() && //
				((isShowingNewbieBots() ? ((FakePlayer) player).getBotMode() == 1 : //
					(isShowingPvPBots() ? ((FakePlayer) player).getBotMode() == 3 : //
						(isShowingTownBots() ? ((FakePlayer) player).getBotMode() == 2 : //
							(isShowingFarmBots() ? ((FakePlayer) player).getBotMode() == 4 : player.isBot()//
							)))))).collect(Collectors.toList());
		}
		
		int size = list.size();
		
		// Calculate page number
		final int max = getMaxPageNumber(list.size(), pageLimit);
		
		page = page > max ? max : page < 1 ? 1 : page;
		// Cut list up to page number
		list = list.subList((page - 1) * pageLimit, Math.min(page * pageLimit, list.size()));
		
		NpcHtmlMessage htm = new NpcHtmlMessage(1);
		htm.setFile(botsFolder + "botmenu.htm");
		TextBuilder replyMSG = new TextBuilder();
		
		int count = 0;
		String type = "";
		
		if (page > 1)
		{
			count = (pageLimit * page - pageLimit);
		}
		
		for (L2PcInstance player : list)
		{
			
			switch (((FakePlayer) player).getBotMode())
			{
				case 1:
					type = "N";
					break;
				case 2:
					type = "T";
					break;
				case 3:
					type = "P";
					break;
				case 4:
					type = "F";
					break;
				case 5:
					type = "FP";
					break;
			}
			
			count++;
			
			replyMSG.append("<table width=300><tr><td width=35 align=right>" + count + ".</td>"//
				+ "<td width=120><a action=\"bypass -h admin_bot_info " + player.getName() + "\">" + player.getName() + "</a></td>"//
				+ "<td width=110>" + player.getTemplate().className + "</td><td width=40>" + type + "</td><td width=40>" + player.getLevel() + "</td>" + "</tr></table>");
			replyMSG.append("<img src=\"L2UI.Squaregray\" width=\"300\" height=\"1\">");
		}
		
		replyMSG.append("<table width=300><tr>");
		replyMSG.append("<td align=left width=100>" + (page > 1 ? "<button value=\"Prev\" action=\"bypass -h admin_bots " + (page - 1) + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "") + "</td>");
		replyMSG.append("<td align=center width=100>Page: " + page + " / " + max + "</td>");
		replyMSG.append("<td align=right width=100>" + (page < max ? "<button value=\"Next\" action=\"bypass -h admin_bots " + (page + 1) + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "") + "</td>");
		replyMSG.append("</tr></table>");
		
		htm.replace("%size%", size);
		htm.replace("%fakecount%", FakePlayerManager.INSTANCE.getFakePlayersCount());
		htm.replace("%taskcount%", FakePlayerTaskManager.INSTANCE.getTaskCount());
		htm.replace("%default%", (isShowByName() || isShowByLvl()) ? "<a action=\"bypass -h admin_show_bots_opion 0\">#</a>" : "#");
		htm.replace("%name%", isShowByName() ? "Name" : "<a action=\"bypass -h admin_show_bots_opion 2\">Name</a>");
		htm.replace("%level%", isShowByLvl() ? "Level" : "<a action=\"bypass -h admin_show_bots_opion 1\">Level</a>");
		
		htm.replace("%newbieBots%", isShowingNewbieBots() ? "<button value=\"\" action=\"bypass admin_show_bots_opion 3\" width=15 height=15 back=\"L2UI.CheckBox\" fore=\"L2UI.CheckBox_checked\">" : "<button value=\"\" action=\"bypass admin_show_bots_opion 3\" width=15 height=15 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox\">");
		htm.replace("%pvpBots%", isShowingPvPBots() ? "<button value=\"\" action=\"bypass admin_show_bots_opion 4\" width=15 height=15 back=\"L2UI.CheckBox\" fore=\"L2UI.CheckBox_checked\">" : "<button value=\"\" action=\"bypass admin_show_bots_opion 4\" width=15 height=15 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox\">");
		htm.replace("%townBots%", isShowingTownBots() ? "<button value=\"\" action=\"bypass admin_show_bots_opion 5\" width=15 height=15 back=\"L2UI.CheckBox\" fore=\"L2UI.CheckBox_checked\">" : "<button value=\"\" action=\"bypass admin_show_bots_opion 5\" width=15 height=15 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox\">");
		htm.replace("%farmBots%", isShowingFarmBots() ? "<button value=\"\" action=\"bypass admin_show_bots_opion 6\" width=15 height=15 back=\"L2UI.CheckBox\" fore=\"L2UI.CheckBox_checked\">" : "<button value=\"\" action=\"bypass admin_show_bots_opion 6\" width=15 height=15 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox\">");
		
		htm.replace("%botsList%", replyMSG.toString());
		activeChar.sendPacket(htm);
	}
	
	public static int getMaxPageNumber(int objectsSize, int pageSize)
	{
		return objectsSize / pageSize + (objectsSize % pageSize == 0 ? 0 : 1);
	}
	
	private void showBotInfo(final L2PcInstance activeChar, L2PcInstance player)
	{
		if (player == null)
		{
			L2Object target = activeChar.getTarget();
			
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
			{
				return;
			}
		}
		else
		{
			activeChar.setTarget(player);
		}
		
		if (player.isBot())
		{
			AdminEditChar.gatherCharacterInfo(activeChar, player, "botinfo.htm");
		}
		else
		{
			AdminEditChar.gatherCharacterInfo(activeChar, player, "charinfo.htm");
		}
	}
	
	private void setShowByLvl(boolean option)
	{
		showByLvl = option;
	}
	
	private boolean isShowByLvl()
	{
		return showByLvl;
	}
	
	private void setShowByName(boolean option)
	{
		showByName = option;
	}
	
	private boolean isShowByName()
	{
		return showByName;
	}
	
	// XXX Types
	private void setNewbieBots(boolean option)
	{
		showNewbieBots = option;
	}
	
	private boolean isShowingNewbieBots()
	{
		return showNewbieBots;
	}
	
	private void setPvPBots(boolean option)
	{
		showPvPBots = option;
	}
	
	private boolean isShowingPvPBots()
	{
		return showPvPBots;
	}
	
	private void setTownBots(boolean option)
	{
		showTownBots = option;
	}
	
	private boolean isShowingTownBots()
	{
		return showTownBots;
	}
	
	private void setFarmBots(boolean option)
	{
		showFarmBots = option;
	}
	
	private boolean isShowingFarmBots()
	{
		return showFarmBots;
	}
}