package l2jorion.game.model.entity.event.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import l2jorion.Config;
import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.cache.HtmCache;
import l2jorion.game.controllers.GameTimeController;
import l2jorion.game.datatables.sql.TeleportLocationTable;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.model.L2TeleportLocation;
import l2jorion.game.model.L2World;
import l2jorion.game.model.Location;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.Announcements;
import l2jorion.game.model.entity.event.manager.EventTask;
import l2jorion.game.model.zone.ZoneId;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.MagicSkillUser;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.game.network.serverpackets.SetupGauge;
import l2jorion.game.powerpack.PowerPackConfig;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.game.util.Broadcast;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

public class Dungeon implements EventTask, IVoicedCommandHandler, ICustomByPassHandler
{
	protected static final Logger LOG = LoggerFactory.getLogger(Dungeon.class);
	
	public static String _eventName = Config.DG_NAME;
	public static boolean _joining = false;
	public static boolean _inProgress = false;
	public static List<L2PcInstance> playersInside = new ArrayList<>();
	private String startEventTime;
	public int _timer = Math.abs(Config.DG_EVENT_TIME * 60);
	public ScheduledFuture<?> _waiterTask;
	
	private class EscapeFinalizer implements Runnable
	{
		L2PcInstance _player;
		L2TeleportLocation _tp;
		
		public EscapeFinalizer(L2PcInstance player, L2TeleportLocation loc)
		{
			_player = player;
			_tp = loc;
		}
		
		@Override
		public void run()
		{
			_player.enableAllSkills();
			
			if (!playersInside.contains((_player)))
			{
				playersInside.add(_player);
			}
			_player.setInstanceId(Config.PVP_ZONE_INSTANCE_ID);
			_player.teleToLocation(_tp.getLocX(), _tp.getLocY(), _tp.getLocZ(), true);
		}
		
	}
	
	public static Dungeon getNewInstance()
	{
		return new Dungeon();
	}
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (activeChar.isInsideZone(ZoneId.ZONE_RANDOM) || activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("You can't use it from this zone.");
			activeChar.sendPacket(new ExShowScreenMessage("You can't use it from this zone.", 2000, 2, false));
			activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
			return false;
		}
		
		if (command.equalsIgnoreCase("boom"))
		{
			showMessageWindow(activeChar);
			return true;
		}
		
		return false;
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if (!is_inProgress())
		{
			player.sendMessage("The zone is not opened yet.");
			player.sendPacket(new ExShowScreenMessage("The zone is not opened yet.", 2000, 2, false));
			player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
			return;
		}
		
		if (player.isInsideZone(ZoneId.ZONE_RANDOM) || player.isInOlympiadMode())
		{
			player.sendMessage("You can't use it from this zone.");
			player.sendPacket(new ExShowScreenMessage("You can't use it from this zone.", 2000, 2, false));
			player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
			return;
		}
		
		int unstuckTimer = PowerPackConfig.GLOBALGK_TIMEOUT * 1000;
		
		if (parameters.startsWith("goto"))
		{
			try
			{
				int locId = Integer.parseInt(parameters.substring(parameters.indexOf(" ") + 1).trim());
				L2TeleportLocation tpPoint = TeleportLocationTable.getInstance().getTemplate(locId);
				
				if (tpPoint != null)
				{
					if (PowerPackConfig.GLOBALGK_PRICE > 0 && player.getLevel() > Config.FREE_TELEPORT_UNTIL + 1)
					{
						if (player.getInventory().getAdena() < PowerPackConfig.GLOBALGK_PRICE)
						{
							player.sendMessage("You don't have enough Adena.");
							player.sendPacket(new ExShowScreenMessage("You don't have enough Adena.", 1000, 2, false));
							player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
							return;
						}
						player.reduceAdena("teleport", PowerPackConfig.GLOBALGK_PRICE, null, true);
					}
					
					if (PowerPackConfig.GLOBALGK_PRICE == -1 && player.getLevel() > Config.FREE_TELEPORT_UNTIL + 1)
					{
						if (player.getInventory().getAdena() < tpPoint.getPrice())
						{
							player.sendMessage("You don't have enough Adena.");
							player.sendPacket(new ExShowScreenMessage("You don't have enough Adena.", 1000, 2, false));
							player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
							return;
						}
						player.reduceAdena("teleport", tpPoint.getPrice(), null, true);
					}
					
					if (unstuckTimer > 0 && !(player.isInsideZone(ZoneId.ZONE_PEACE)))
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						
						player.setTarget(player);
						player.broadcastPacket(new MagicSkillUser(player, 1050, 1, unstuckTimer, 0));
						player.sendPacket(new SetupGauge(0, unstuckTimer));
						player.setTarget(null);
						
						player.setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(new EscapeFinalizer(player, tpPoint), unstuckTimer));
						player.setSkillCastEndTime(10 + GameTimeController.getInstance().getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);
						
						return;
					}
					
					if (!playersInside.contains((player)))
					{
						playersInside.add(player);
					}
					player.setInstanceId(Config.PVP_ZONE_INSTANCE_ID);
					player.teleToLocation(tpPoint.getLocX(), tpPoint.getLocY(), tpPoint.getLocZ(), true);
					return;
				}
				player.sendMessage("Teleport, with ID " + locId + " does not exist in the database");
			}
			catch (Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		else if (parameters.startsWith("exit"))
		{
			if (player.getInstanceId() == 0)
			{
				player.sendMessage("You're not in zone.");
				return;
			}
			
			player.setInstanceId(0);
			if (playersInside.contains((player)))
			{
				playersInside.remove(player);
			}
			
			player.teleToLocation(new Location(83431, 148331, -3400), 50, false);
		}
	}
	
	public static void showMessageWindow(L2PcInstance player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(player.getLastQuestNpcObject());
		String text = HtmCache.getInstance().getHtm("data/html/mods/dungeon/main.htm");
		htm.setHtml(text);
		player.sendPacket(htm);
	}
	
	public void setEventStartTime(final String newTime)
	{
		startEventTime = newTime;
	}
	
	@Override
	public String getEventIdentifier()
	{
		return _eventName;
	}
	
	@Override
	public void run()
	{
		LOG.info("Dungeon: Event notification start");
		eventOnceStart();
	}
	
	public String get_eventName()
	{
		return _eventName;
	}
	
	@Override
	public String getEventStartTime()
	{
		return startEventTime;
	}
	
	public void eventOnceStart()
	{
		if (startJoin() && _waiterTask == null)
		{
			_timer = Math.abs(Config.DG_EVENT_TIME * 60);
			_waiterTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new timer(), 0, 1000);
		}
	}
	
	public void abortEvent()
	{
		_joining = false;
		_inProgress = false;
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": the zone closed!");
	}
	
	public boolean startJoin()
	{
		_inProgress = true;
		_joining = true;
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": Zone is opened for " + (Config.DG_EVENT_TIME) + " minutes");
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": Info command .boom");
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if (player != null)
			{
				if (player.isOnline() != 0)
				{
					showMessageWindow(player);
				}
			}
		}
		
		return true;
	}
	
	private class timer implements Runnable
	{
		protected timer()
		{
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_timer > 0)
				{
					_timer--;
				}
				
				switch (_timer)
				{
					case 7200:
						Broadcast.toAllOnlinePlayers(_eventName + ": " + (_timer / 60) / 60 + " hours till zone close!", true);
						break;
					case 3600:
						Broadcast.toAllOnlinePlayers(_eventName + ": " + (_timer / 60) / 60 + " hour till zone close!", true);
						break;
					case 1800:
					case 900:
					case 600:
					case 300:
						Broadcast.toAllOnlinePlayers(_eventName + ": " + _timer / 60 + " minutes till zone close!", true);
						break;
					case 60:
						Broadcast.toAllOnlinePlayers(_eventName + ": " + _timer / 60 + " minute till zone close!", true);
						break;
					case 30:
					case 15:
						Broadcast.toAllOnlinePlayers(_eventName + ": " + _timer + " seconds till zone close!", true);
						break;
					case 5:
					case 4:
					case 3:
					case 2:
						Broadcast.toAllOnlinePlayers(_eventName + ": " + _timer + " seconds till zone close!", true);
						break;
					case 1:
						Broadcast.toAllOnlinePlayers(_eventName + ": " + _timer + " second till zone close!", true);
						break;
					case 0:
						abortEvent();
						getPlayers().stream().forEach(player -> player.setInstanceId(0));
						getPlayers().stream().forEach(player -> player.teleToLocation(new Location(185412, 20475, -3265), 50, false));
						getPlayers().clear();
						if (_waiterTask != null && _timer == 0)
						{
							_waiterTask.cancel(false);
							_waiterTask = null;
						}
						break;
				}
			}
			catch (Exception e)
			{
				LOG.warn("Dungeon: Error in timer: " + e.getMessage(), e);
			}
		}
	}
	
	public boolean is_inProgress()
	{
		return _inProgress;
	}
	
	@Override
	public String[] getByPassCommands()
	{
		return new String[]
		{
			"dg"
		};
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]
		{
			"boom"
		};
	}
	
	public List<L2PcInstance> getPlayers()
	{
		return playersInside;
	}
}