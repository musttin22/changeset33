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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import l2jorion.Config;
import l2jorion.game.community.manager.MailBBSManager;
import l2jorion.game.datatables.sql.CharNameTable;
import l2jorion.game.datatables.sql.ClanTable;
import l2jorion.game.model.L2Clan;
import l2jorion.game.model.L2World;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.Announcements;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.taskmanager.Task;
import l2jorion.game.taskmanager.TaskManager;
import l2jorion.game.taskmanager.TaskManager.ExecutedTask;
import l2jorion.game.taskmanager.TaskTypes;
import l2jorion.game.templates.L2Item;
import l2jorion.game.util.Util;
import l2jorion.log.Log;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.L2DatabaseFactory;

public final class TaskWeeklyBoard extends Task
{
	private int rewardId = 9997; // Premium 1 day
	private int rewardAmount = 1;
	private String PvpWinner = "-";
	private String PkWinner = "-";
	private String FarmWinner = "-";
	private String ClanWinner = "-";
	private String OnlineWinner = "-";
	
	public static final String NAME = "weeklyBoard_reset";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.DAY_OF_WEEK) == Integer.valueOf(task.getParams()[0]))
		{
			getTopPvpAndAddReward();
			getTopPkAndAddReward();
			getTopFarmAndAddReward();
			getTopClanRaidPointsAndAddReward();
			getTopOnlineAndAddReward();
			
			Announcements.getInstance().announceToAll("Top Weekly Event winners: [PvP:" + PvpWinner + "] [PK:" + PkWinner + "] [Farm:" + FarmWinner + "] [Clan:" + ClanWinner + "] [Online:" + OnlineWinner + "]. Congratulations!");
			
			deleteWeeklyTable();
			
			Log.add("Reset activated", "Reset");
		}
	}
	
	private void getTopPvpAndAddReward()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT obj_Id, char_name, pvp_kills FROM weeklyBoard WHERE pvp_kills > 0 order by pvp_kills desc limit 1");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				String name = rset.getString("char_name");
				
				L2PcInstance player = L2World.getInstance().getPlayer(name);
				if (player == null)
				{
					player = L2PcInstance.load(CharNameTable.getInstance().getIdByName(name));
				}
				
				if (player != null)
				{
					player.getInventory().addItem("Event", rewardId, rewardAmount, player, null);
					MailBBSManager.getInstance().sendMail(player.getName(), "Weekly Event", "" + player.getName() + " you won The Weekly Event - Top PvP! " + "You've got: " + Util.formatAdena(rewardAmount) + " " + L2Item.getItemNameById(rewardId) + ". Check your inventory!", player, true);
					
					if (rewardAmount > 1)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(rewardId);
						sm.addNumber(rewardAmount);
						player.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
						sm.addItemName(rewardId);
						player.sendPacket(sm);
					}
					
					PvpWinner = player.getName();
					
					// Remove offline character from the list
					if (player.isOnline() == 0)
					{
						player.deleteMe();
					}
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOG.info(getClass().getSimpleName() + ": Couldn't get most pvp.");
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void getTopPkAndAddReward()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT obj_Id, char_name, pk_kills FROM weeklyBoard WHERE pk_kills > 0 order by pk_kills desc limit 1");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				String name = rset.getString("char_name");
				
				L2PcInstance player = L2World.getInstance().getPlayer(name);
				if (player == null)
				{
					player = L2PcInstance.load(CharNameTable.getInstance().getIdByName(name));
				}
				
				if (player != null)
				{
					player.getInventory().addItem("Event", rewardId, rewardAmount, player, null);
					MailBBSManager.getInstance().sendMail(player.getName(), "Weekly Event", "" + player.getName() + " you won The Weekly Event - Top Pk! " + "You've got: " + Util.formatAdena(rewardAmount) + " " + L2Item.getItemNameById(rewardId) + ". Check your inventory!", player, true);
					
					if (rewardAmount > 1)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(rewardId);
						sm.addNumber(rewardAmount);
						player.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
						sm.addItemName(rewardId);
						player.sendPacket(sm);
					}
					
					PkWinner = player.getName();
					
					// Remove offline character from the list
					if (player.isOnline() == 0)
					{
						player.deleteMe();
					}
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOG.info(getClass().getSimpleName() + ": Couldn't get most pk.");
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void getTopFarmAndAddReward()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT obj_Id, char_name, farm_kills FROM weeklyBoard WHERE farm_kills > 0 order by farm_kills desc limit 1");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				String name = rset.getString("char_name");
				
				L2PcInstance player = L2World.getInstance().getPlayer(name);
				if (player == null)
				{
					player = L2PcInstance.load(CharNameTable.getInstance().getIdByName(name));
				}
				
				if (player != null)
				{
					player.getInventory().addItem("Event", rewardId, rewardAmount, player, null);
					MailBBSManager.getInstance().sendMail(player.getName(), "Weekly Event", "" + player.getName() + " you won The Weekly Event - Top Farm! " + "You've got: " + Util.formatAdena(rewardAmount) + " " + L2Item.getItemNameById(rewardId) + ". Check your inventory!", player, true);
					
					if (rewardAmount > 1)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(rewardId);
						sm.addNumber(rewardAmount);
						player.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
						sm.addItemName(rewardId);
						player.sendPacket(sm);
					}
					
					FarmWinner = player.getName();
					
					// Remove offline character from the list
					if (player.isOnline() == 0)
					{
						player.deleteMe();
					}
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOG.info(getClass().getSimpleName() + ": Couldn't get most farm.");
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void getTopClanRaidPointsAndAddReward()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT clan_name, SUM(raid_points) AS total FROM weeklyBoard WHERE raid_points > 0 GROUP BY clan_name ORDER BY total DESC LIMIT 1");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				String name = rset.getString("clan_name");
				
				if (name == null || name.isEmpty())
				{
					continue;
				}
				
				L2Clan clan = ClanTable.getInstance().getClanByName(name);
				
				L2PcInstance player = L2World.getInstance().getPlayer(clan.getLeaderName());
				if (player == null)
				{
					player = L2PcInstance.load(CharNameTable.getInstance().getIdByName(clan.getLeaderName()));
				}
				
				if (player != null)
				{
					player.getInventory().addItem("Event", rewardId, rewardAmount, player, null);
					MailBBSManager.getInstance().sendMail(player.getName(), "Weekly Event", "" + player.getName() + " your clan won The Weekly Event - Raid Points! " + "You've got: " + Util.formatAdena(rewardAmount) + " " + L2Item.getItemNameById(rewardId) + ". Check your inventory!", player, true);
					
					if (rewardAmount > 1)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(rewardId);
						sm.addNumber(rewardAmount);
						player.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
						sm.addItemName(rewardId);
						player.sendPacket(sm);
					}
					
					ClanWinner = player.getName();
					
					// Remove offline character from the list
					if (player.isOnline() == 0)
					{
						player.deleteMe();
					}
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOG.info(getClass().getSimpleName() + ": Couldn't get most raid_points.");
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void getTopOnlineAndAddReward()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT obj_Id, char_name, online_time FROM weeklyBoard WHERE online_time > 0 order by online_time desc limit 1");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				String name = rset.getString("char_name");
				
				L2PcInstance player = L2World.getInstance().getPlayer(name);
				if (player == null)
				{
					player = L2PcInstance.load(CharNameTable.getInstance().getIdByName(name));
				}
				
				if (player != null)
				{
					player.getInventory().addItem("Event", rewardId, rewardAmount, player, null);
					MailBBSManager.getInstance().sendMail(player.getName(), "Weekly Event", "" + player.getName() + " you won The Weekly Event - Top Online! " + "You've got: " + Util.formatAdena(rewardAmount) + " " + L2Item.getItemNameById(rewardId) + ". Check your inventory!", player, true);
					
					if (rewardAmount > 1)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(rewardId);
						sm.addNumber(rewardAmount);
						player.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
						sm.addItemName(rewardId);
						player.sendPacket(sm);
					}
					
					OnlineWinner = player.getName();
					
					// Remove offline character from the list
					if (player.isOnline() == 0)
					{
						player.deleteMe();
					}
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOG.info(getClass().getSimpleName() + ": Couldn't get most online time.");
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void deleteWeeklyTable()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			Statement st = con.createStatement();
			ResultSet rset = st.executeQuery("TRUNCATE weeklyBoard");
			rset.close();
			st.close();
		}
		catch (SQLException e)
		{
			LOG.info(getClass().getSimpleName() + ": Couldn't delete weeklyBoard table.");
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		PvpWinner = "-";
		PkWinner = "-";
		FarmWinner = "-";
		ClanWinner = "-";
		OnlineWinner = "-";
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		
		if (Config.L2LIMIT_CUSTOM)
		{
			TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "15:00:00", "");
		}
		// it's for test
		// TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "60000", "60000", "");
	}
}