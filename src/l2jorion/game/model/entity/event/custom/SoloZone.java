/*
 * L2jOrion Project - www.l2jorion.com 
 * 
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
package l2jorion.game.model.entity.event.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javolution.text.TextBuilder;
import l2jorion.Config;
import l2jorion.game.datatables.sql.NpcTable;
import l2jorion.game.datatables.sql.SpawnTable;
import l2jorion.game.managers.CastleManager;
import l2jorion.game.model.Location;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.Announcements;
import l2jorion.game.model.entity.event.manager.EventTask;
import l2jorion.game.model.entity.siege.Castle;
import l2jorion.game.model.olympiad.Olympiad;
import l2jorion.game.model.olympiad.OlympiadManager;
import l2jorion.game.model.spawn.L2Spawn;
import l2jorion.game.network.serverpackets.ActionFailed;
import l2jorion.game.network.serverpackets.MagicSkillUser;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.templates.L2NpcTemplate;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.DatabaseUtils;
import l2jorion.util.database.L2DatabaseFactory;
import l2jorion.util.random.Rnd;

public class SoloZone implements EventTask
{
	protected static final Logger LOG = LoggerFactory.getLogger(SoloZone.class);
	
	protected static String _eventName = new String(), _eventDesc = new String(), _joiningLocationName = new String();
	private static L2Spawn _npcSpawn;
	protected static boolean _joining = false, _teleport = false, _started = false, _aborted = false, _inProgress = false;
	protected static int _npcId = 0, _npcX = 0, _npcY = 0, _npcZ = 0, _playerX = 0, _playerY = 0, _playerZ = 0, _npcHeading = 0, _joinTime = 0, _eventTime = 0, _instanceZoneId = 0;
	private String startEventTime;
	
	public static Vector<L2PcInstance> _players = new Vector<>();
	public static Vector<String> _savePlayers = new Vector<>();
	private final static List<L2Spawn> _eventMobSpawn = new ArrayList<>();
	
	public static SoloZone getNewInstance()
	{
		return new SoloZone();
	}
	
	public static String get_eventName()
	{
		return _eventName;
	}
	
	public static boolean set_eventName(final String _eventName)
	{
		if (!is_inProgress())
		{
			SoloZone._eventName = _eventName;
			return true;
		}
		return false;
	}
	
	public static String get_eventDesc()
	{
		return _eventDesc;
	}
	
	public static boolean set_eventDesc(final String _eventDesc)
	{
		if (!is_inProgress())
		{
			SoloZone._eventDesc = _eventDesc;
			return true;
		}
		return false;
	}
	
	public static String get_joiningLocationName()
	{
		return _joiningLocationName;
	}
	
	public static boolean set_joiningLocationName(final String _joiningLocationName)
	{
		if (!is_inProgress())
		{
			SoloZone._joiningLocationName = _joiningLocationName;
			return true;
		}
		return false;
	}
	
	public static int get_npcId()
	{
		return _npcId;
	}
	
	public static boolean set_npcId(final int _npcId)
	{
		if (!is_inProgress())
		{
			SoloZone._npcId = _npcId;
			return true;
		}
		return false;
	}
	
	public static Location get_npcLocation()
	{
		final Location npc_loc = new Location(_npcX, _npcY, _npcZ, _npcHeading);
		
		return npc_loc;
		
	}
	
	public static int get_joinTime()
	{
		return _joinTime;
	}
	
	public static boolean set_joinTime(final int _joinTime)
	{
		if (!is_inProgress())
		{
			SoloZone._joinTime = _joinTime;
			return true;
		}
		return false;
	}
	
	public static int get_eventTime()
	{
		return _eventTime;
	}
	
	public static boolean set_eventTime(final int _eventTime)
	{
		if (!is_inProgress())
		{
			SoloZone._eventTime = _eventTime;
			return true;
		}
		return false;
	}
	
	public String getStartEventTime()
	{
		return startEventTime;
	}
	
	public boolean setStartEventTime(final String startEventTime)
	{
		if (!is_inProgress())
		{
			this.startEventTime = startEventTime;
			return true;
		}
		return false;
	}
	
	public static boolean is_joining()
	{
		return _joining;
	}
	
	public static boolean is_teleport()
	{
		return _teleport;
	}
	
	public static boolean is_started()
	{
		return _started;
	}
	
	public static boolean is_aborted()
	{
		return _aborted;
	}
	
	public static boolean is_inProgress()
	{
		return _inProgress;
	}
	
	public static boolean checkStartJoinOk()
	{
		if (_started || _teleport || _joining || _eventName.equals("") || _joiningLocationName.equals("") || _eventDesc.equals("") || _npcId == 0 || _npcX == 0 || _npcY == 0 || _npcZ == 0)
		{
			return false;
		}
		
		if (!checkStartJoinPlayerInfo())
		{
			return false;
		}
		
		if (!Config.ALLOW_EVENTS_DURING_OLY && Olympiad.getInstance().inCompPeriod())
		{
			return false;
		}
		
		for (final Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle != null && castle.getSiege() != null && castle.getSiege().getIsInProgress())
			{
				return false;
			}
		}
		
		if (!checkOptionalEventStartJoinOk())
		{
			return false;
		}
		
		return true;
	}
	
	private static boolean checkStartJoinPlayerInfo()
	{
		return true;
		
	}
	
	protected static boolean checkAutoEventStartJoinOk()
	{
		if (_joinTime == 0 || _eventTime == 0)
		{
			return false;
		}
		
		return true;
	}
	
	private static boolean checkOptionalEventStartJoinOk()
	{
		return true;
		
	}
	
	public static void setNpcPos(final L2PcInstance activeChar)
	{
		_npcX = activeChar.getX();
		_npcY = activeChar.getY();
		_npcZ = activeChar.getZ();
		_npcHeading = activeChar.getHeading();
	}
	
	private static void spawnEventNpc()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_npcId);
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
			_npcSpawn.setLocx(_npcX);
			_npcSpawn.setLocy(_npcY);
			_npcSpawn.setLocz(_npcZ);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(_npcHeading);
			_npcSpawn.setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			
			_npcSpawn.init();
			_npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			_npcSpawn.getLastSpawn().setTitle(_eventName);
			_npcSpawn.getLastSpawn()._isSoloZoneMob = true;
			_npcSpawn.getLastSpawn().isAggressive();
			_npcSpawn.getLastSpawn().decayMe();
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOG.warn(_eventName + " Engine[spawnEventNpc(exception: " + e.getMessage());
		}
	}
	
	private static void unspawnEventNpc()
	{
		if (_npcSpawn == null || _npcSpawn.getLastSpawn() == null)
		{
			return;
		}
		
		_npcSpawn.getLastSpawn().deleteMe();
		_npcSpawn.stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
	}
	
	public static boolean startJoin()
	{
		if (!checkStartJoinOk())
		{
			if (Config.DEBUG)
			{
				LOG.warn(_eventName + " Engine[startJoin]: startJoinOk() = false");
			}
			return false;
		}
		
		_inProgress = true;
		_joining = true;
		spawnEventMobs();
		spawnEventNpc();
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + _eventDesc);
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": Join in " + _joiningLocationName);
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + _joinTime + " minutes till zone open");
		// Announcements.getInstance().gameAnnounceToAll(_eventName + ": Commands: .join .leave .eventinfo");
		return true;
	}
	
	public static boolean startTeleport()
	{
		if (!_joining || _started || _teleport)
		{
			return false;
		}
		
		_joining = false;
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": Teleport to spot in 5 seconds");
		
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized (_players)
				{
					for (final L2PcInstance player : _players)
					{
						if (player != null)
						{
							player.setInstanceId(_instanceZoneId);
							player.teleToLocation(_playerX + Rnd.get(201) - 100, _playerY + Rnd.get(201) - 100, _playerZ);
						}
					}
				}
				
			}
		}, 5000);
		_teleport = true;
		return true;
	}
	
	public static boolean startEvent()
	{
		if (!startEventOk())
		{
			return false;
		}
		
		_teleport = false;
		_started = true;
		
		return true;
	}
	
	public static void finishEvent()
	{
		if (!finishEventOk())
		{
			return;
		}
		
		_started = false;
		_aborted = false;
		unspawnEventMobs();
		unspawnEventNpc();
		teleportFinish();
	}
	
	public static void abortEvent()
	{
		if (!_joining && !_teleport && !_started)
		{
			return;
		}
		
		if (_joining && !_teleport && !_started)
		{
			unspawnEventMobs();
			unspawnEventNpc();
			cleanSoloZone();
			_joining = false;
			_inProgress = false;
			Announcements.getInstance().gameAnnounceToAll(_eventName + ": The Event cancelled");
			return;
		}
		
		_joining = false;
		_teleport = false;
		_started = false;
		_aborted = true;
		unspawnEventMobs();
		unspawnEventNpc();
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": The Event cancelled");
		teleportFinish();
	}
	
	public static void teleportFinish()
	{
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": Teleport back to participation NPC in 5 seconds");
		
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized (_players)
				{
					
					for (final L2PcInstance player : _players)
					{
						if (player != null)
						{
							if (player.isOnline() != 0)
							{
								player.setInstanceId(0);
								player.teleToLocation(_npcX, _npcY, _npcZ, false);
							}
							else
							{
								Connection con = null;
								try
								{
									con = L2DatabaseFactory.getInstance().getConnection();
									final PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE char_name=?");
									statement.setInt(1, _npcX);
									statement.setInt(2, _npcY);
									statement.setInt(3, _npcZ);
									statement.setString(4, player.getName());
									statement.execute();
									DatabaseUtils.close(statement);
								}
								catch (final Exception e)
								{
									if (Config.ENABLE_ALL_EXCEPTIONS)
									{
										e.printStackTrace();
									}
									
									LOG.error(e.getMessage(), e);
								}
								finally
								{
									CloseUtil.close(con);
								}
							}
						}
					}
					
				}
				cleanSoloZone();
			}
		}, 5000);
	}
	
	public static void eventOnceStart()
	{
		if (startJoin() && !_aborted)
		{
			if (_joinTime > 0)
			{
				waiter(_joinTime * 60 * 1000); // minutes for join event
			}
			else if (_joinTime <= 0)
			{
				abortEvent();
				return;
			}
			if (startTeleport() && !_aborted)
			{
				// waiter(1 * 60 * 1000); // 1 min wait time untill start fight after teleported
				if (startEvent() && !_aborted)
				{
					waiter(_eventTime * 60 * 1000); // minutes for event time
					finishEvent();
				}
			}
			else if (!_aborted)
			{
				abortEvent();
			}
		}
		
	}
	
	protected static void waiter(final long interval)
	{
		final long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);
		
		while (startWaiterTime + interval > System.currentTimeMillis() && !_aborted)
		{
			seconds--; // Here because we don't want to see two time announce at the same time
			
			if (_joining || _started || _teleport)
			{
				switch (seconds)
				{
					case 3600: // 1 hour left
						if (_joining)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": Join in " + _joiningLocationName + "");
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds / 60 / 60 + " hour till zone open");
						}
						else if (_started)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds / 60 / 60 + " hour till event finish");
						}
						
						break;
					case 1800: // 30 minutes left
					case 900: // 15 minutes left
					case 600: // 10 minutes left
					case 300: // 5 minutes left
					case 240: // 4 minutes left
					case 180: // 3 minutes left
					case 120: // 2 minutes left
						if (_joining)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": Join in " + _joiningLocationName + "");
							// Announcements.getInstance().gameAnnounceToAll(_eventName + ": Commands: .join .leave .eventinfo");
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds / 60 + " minutes till zone open");
						}
						else if (_started)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds / 60 + " minutes till zone close");
						}
						break;
					case 60: // 1 minute left
						
						if (_joining)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": Join in " + _joiningLocationName + "");
							// Announcements.getInstance().gameAnnounceToAll(_eventName + ": Commands: .join .leave .eventinfo");
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds / 60 + " minute till zone open");
						}
						else if (_started)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds / 60 + " minute till zone close");
						}
						break;
					case 30: // 30 seconds left
					case 15: // 15 seconds left
					case 10: // 10 seconds left
					case 3: // 3 seconds left
					case 2: // 2 seconds left
						if (_joining)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds + " seconds till zone open");
						}
						else if (_started)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds + " seconds till zone close");
						}
						break;
					case 1: // 1 seconds left
						
						if (_joining)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds + " second till zone open");
						}
						else if (_started)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds + " second till zone close");
						}
						break;
				}
			}
			
			final long startOneSecondWaiterStartTime = System.currentTimeMillis();
			while (startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (final InterruptedException ie)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						ie.printStackTrace();
					}
				}
			}
		}
	}
	
	private static boolean startEventOk()
	{
		if (_joining || !_teleport || _started)
		{
			return false;
		}
		
		return true;
	}
	
	private static boolean finishEventOk()
	{
		if (!_started)
		{
			return false;
		}
		
		return true;
	}
	
	private static void unspawnEventMobs()
	{
		for (L2Spawn mob : _eventMobSpawn)
		{
			mob.getLastSpawn().deleteMe();
			mob.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(_npcSpawn, false);
		}
	}
	
	private static void spawnEventMobs()
	{
		for (L2Spawn mob : _eventMobSpawn)
		{
			SpawnTable.getInstance().addNewSpawn(mob, false);
			mob.init();
		}
	}
	
	public static void loadEventMobsData()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM solo_zone_mobs");
			ResultSet rs = statement.executeQuery();
			
			L2Spawn spawn;
			L2NpcTemplate template;
			
			while (rs.next())
			{
				template = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
				if (template != null)
				{
					spawn = new L2Spawn(template);
					spawn.setId(rs.getInt("id"));
					spawn.setAmount(1);
					spawn.setInstanceId(_instanceZoneId);
					spawn.setLocx(rs.getInt("x"));
					spawn.setLocy(rs.getInt("y"));
					spawn.setLocz(rs.getInt("z"));
					spawn.setHeading(rs.getInt("heading"));
					spawn.setRespawnDelay(rs.getInt("respawnDelay"));
					_eventMobSpawn.add(spawn);
				}
				else
				{
					LOG.error("Missing npc data in npc table for id: " + rs.getInt("npcId"));
				}
			}
			rs.close();
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOG.error("Error loading mobs for the event " + _eventName + ":" + e.getMessage());
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public static void loadData()
	{
		_eventName = new String();
		_eventDesc = new String();
		_joiningLocationName = new String();
		_savePlayers = new Vector<>();
		synchronized (_players)
		{
			_players.clear();
		}
		
		_joining = false;
		_teleport = false;
		_started = false;
		_aborted = false;
		_inProgress = false;
		
		_npcId = 0;
		_npcX = 0;
		_npcY = 0;
		_npcZ = 0;
		_npcHeading = 0;
		_playerX = 0;
		_playerY = 0;
		_playerZ = 0;
		_joinTime = 0;
		_eventTime = 0;
		_instanceZoneId = 0;
		
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("SELECT * from solo_zone");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				_eventName = rs.getString("eventName");
				_eventDesc = rs.getString("eventDesc");
				_joiningLocationName = rs.getString("joiningLocation");
				_npcId = rs.getInt("npcId");
				_npcX = rs.getInt("npcX");
				_npcY = rs.getInt("npcY");
				_npcZ = rs.getInt("npcZ");
				_npcHeading = rs.getInt("npcHeading");
				_playerX = rs.getInt("playerX");
				_playerY = rs.getInt("playerY");
				_playerZ = rs.getInt("playerZ");
				_joinTime = rs.getInt("joinTime");
				_eventTime = rs.getInt("eventTime");
				_instanceZoneId = rs.getInt("instanceZoneId");
			}
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOG.error("Exception loadData: " + e.getMessage());
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public static void saveData()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			
			statement = con.prepareStatement("DELETE from solo_zone");
			statement.execute();
			DatabaseUtils.close(statement);
			
			statement = con.prepareStatement("INSERT INTO solo_zone (eventName, eventDesc, joiningLocation, npcId, npcX, npcY, npcZ, npcHeading, playerX, playerY, playerZ, joinTime, eventTime, instanceZoneId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setString(1, _eventName);
			statement.setString(2, _eventDesc);
			statement.setString(3, _joiningLocationName);
			statement.setInt(4, _npcId);
			statement.setInt(5, _npcX);
			statement.setInt(6, _npcY);
			statement.setInt(7, _npcZ);
			statement.setInt(8, _npcHeading);
			statement.setInt(9, _playerX);
			statement.setInt(10, _playerY);
			statement.setInt(11, _playerZ);
			statement.setInt(12, _joinTime);
			statement.setInt(13, _eventTime);
			statement.setInt(14, _instanceZoneId);
			statement.execute();
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOG.error("Exception: saveData: " + e.getMessage());
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public static void showEventHtml(final L2PcInstance eventPlayer, final String objectId)
	{
		try
		{
			int joinedPlayers = _players.size();
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(1);
			
			final TextBuilder replyMSG = new TextBuilder("<html><title>" + _eventName + "</title><body>");
			replyMSG.append("<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><br1>");
			replyMSG.append("<center><font color=\"3366CC\">" + _eventName + "</font></center><br>");
			replyMSG.append("<center>Description: <font color=\"00FF00\">" + _eventDesc + "</font></center><br1>");
			replyMSG.append("<center>Joined players: " + (joinedPlayers == 0 ? "<font color=FF0000>" + joinedPlayers + "</font>" : "<font color=00FF00>" + joinedPlayers + "</font>") + "</center>");
			replyMSG.append("<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><br><br>");
			
			synchronized (_players)
			{
				if (!_started && !_joining)
				{
					replyMSG.append("<center>Not started yet.</center>");
				}
				else if (!_started && _joining)
				{
					if (_players.contains(eventPlayer))
					{
						replyMSG.append("<center><font color=\"LEVEL\">You joined already.</font><br>");
						
						replyMSG.append("<table width=200 border=\"0\"><tr>");
						replyMSG.append("<td><center><button value=\"Leave\" action=\"bypass -h npc_" + objectId + "_solozone_player_leave\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td>");
						replyMSG.append("</tr></table></center>");
					}
					else
					{
						replyMSG.append("<center><font color=\"LEVEL\">Join now and you will be teleported automatically.</font></center><br>");
						replyMSG.append("<center><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_solozone_player_join\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center><br>");
					}
				}
				else if (_started && !_joining)
				{
					replyMSG.append("<center><font color=\"LEVEL\">" + _eventName + " is open now.</font><br></center>");
					if (_players.contains(eventPlayer))
					{
						if (eventPlayer.isInSoloZone())
						{
							replyMSG.append("<center><button value=\"Leave\" action=\"bypass -h npc_" + objectId + "_solozone_player_leave\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
						}
						else
						{
							replyMSG.append("<center><button value=\"Enter\" action=\"bypass -h npc_" + objectId + "_solozone_player_enter\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center><br>");
						}
					}
					else
					{
						replyMSG.append("<center><button value=\"Enter\" action=\"bypass -h npc_" + objectId + "_solozone_player_enter\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center><br>");
					}
				}
			}
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
			eventPlayer.sendPacket(ActionFailed.STATIC_PACKET);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOG.error(_eventName + " Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}
	
	public static void teleporBack(final L2PcInstance player)
	{
		if (_started && !_joining)
		{
			if (_players.contains(player))
			{
				player.setInSoloZone(true);
				player.setInstanceId(_instanceZoneId);
				player.teleToLocation(_playerX + Rnd.get(201) - 100, _playerY + Rnd.get(201) - 100, _playerZ);
			}
			else
			{
				addPlayer(player);
				player.setInstanceId(_instanceZoneId);
				player.teleToLocation(_playerX + Rnd.get(201) - 100, _playerY + Rnd.get(201) - 100, _playerZ);
			}
		}
	}
	
	public static void addPlayer(final L2PcInstance player)
	{
		if (!addPlayerOk(player))
		{
			return;
		}
		
		synchronized (_players)
		{
			_players.add(player);
		}
		
		player.setInSoloZone(true);
		_savePlayers.add(player.getName());
		player.sendMessage(_eventName + ": You successfully registered to the zone.");
	}
	
	private static boolean addPlayerOk(final L2PcInstance eventPlayer)
	{
		if (eventPlayer.isInSoloZone())
		{
			eventPlayer.sendMessage("You already participated in the zone!");
			return false;
		}
		
		if (eventPlayer._inEventDM || eventPlayer._inEventCTF || eventPlayer._inEventTvT)
		{
			eventPlayer.sendMessage("You already participated to another event!");
			return false;
		}
		
		if (OlympiadManager.getInstance().isRegistered(eventPlayer) || eventPlayer.isInOlympiadMode())
		{
			eventPlayer.sendMessage("You already participated in Olympiad!");
			return false;
		}
		
		synchronized (_players)
		{
			if (_players.contains(eventPlayer))
			{
				eventPlayer.sendMessage("You already participated in the zone!");
				return false;
			}
			
			for (final L2PcInstance player : _players)
			{
				if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer.sendMessage("You already participated in the zone!");
					return false;
				}
				else if (player.getName().equalsIgnoreCase(eventPlayer.getName()))
				{
					eventPlayer.sendMessage("You already participated in the zone!");
					return false;
				}
			}
			
		}
		
		return true;
	}
	
	public static void removePlayer(final L2PcInstance player)
	{
		if (player != null)
		{
			player.setInSoloZone(false);
			
			synchronized (_players)
			{
				_players.remove(player);
			}
			
			player.sendMessage("Your participation in the zone has been removed.");
			if (_started)
			{
				player.setInstanceId(0);
				player.teleToLocation(_npcX, _npcY, _npcZ);
			}
		}
	}
	
	public static void cleanSoloZone()
	{
		synchronized (_players)
		{
			for (final L2PcInstance player : _players)
			{
				if (player != null)
				{
					if (_savePlayers.contains(player.getName()))
					{
						_savePlayers.remove(player.getName());
					}
					player.setInSoloZone(false);
				}
			}
			_players.clear();
		}
		
		_savePlayers = new Vector<>();
		_inProgress = false;
		
		// loadData();
	}
	
	public static synchronized void addDisconnectedPlayer(final L2PcInstance player)
	{
		synchronized (_players)
		{
			if (!_players.contains(player) && _savePlayers.contains(player.getName()))
			{
				_players.add(player);
				player.setInSoloZone(true);
				if (_teleport || _started)
				{
					player.setInstanceId(_instanceZoneId);
					player.teleToLocation(_playerX + Rnd.get(201) - 100, _playerY + Rnd.get(201) - 100, _playerZ);
				}
			}
		}
	}
	
	@Override
	public void run()
	{
		LOG.info(_eventName + ": Event notification start");
		eventOnceStart();
	}
	
	@Override
	public String getEventIdentifier()
	{
		return _eventName;
	}
	
	@Override
	public String getEventStartTime()
	{
		return startEventTime;
	}
	
	public void setEventStartTime(final String newTime)
	{
		startEventTime = newTime;
	}
	
	public static void onDisconnect(L2PcInstance player)
	{
		if (player.isInSoloZone())
		{
			player.setInSoloZone(false);
			
			synchronized (_players)
			{
				_players.remove(player);
			}
			player.setInstanceId(0);
			player.setXYZ(_npcX, _npcY, _npcZ);
		}
	}
	
	public static void kickPlayerFromSoloZone(final L2PcInstance playerToKick)
	{
		if (playerToKick == null)
		{
			return;
		}
		
		synchronized (_players)
		{
			if (_joining)
			{
				_players.remove(playerToKick);
				playerToKick.setInSoloZone(false);
			}
		}
		
		if (_started || _teleport)
		{
			removePlayer(playerToKick);
			if (playerToKick.isOnline() != 0)
			{
				playerToKick.sendMessage("You have been kicked from " + _eventName + ".");
				playerToKick.setInstanceId(0);
				playerToKick.teleToLocation(_npcX + Rnd.get(201) - 100, _npcY + Rnd.get(201) - 100, _npcZ, false);
			}
		}
	}
}