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
import l2jorion.game.datatables.sql.CharNameTable;
import l2jorion.game.datatables.sql.ClanTable;
import l2jorion.game.model.L2Clan;
import l2jorion.game.model.L2World;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.taskmanager.Task;
import l2jorion.game.taskmanager.TaskManager;
import l2jorion.game.taskmanager.TaskManager.ExecutedTask;
import l2jorion.game.taskmanager.TaskTypes;
import l2jorion.log.Log;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.L2DatabaseFactory;

public final class TaskWeeklyTopBoard extends Task
{
	private static int rewardId = 4037;
	private int rewardAmount = 25;
	
	public static final String NAME = "weekly_reset";
	
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
			getTopClanWarAndAddReward();
			getTopClanRaidPointsAndAddReward();
			getTopFisherAndAddReward();
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
			PreparedStatement statement = con.prepareStatement("SELECT obj_Id, char_name, pvpkills FROM weekly_top_board WHERE pvpkills > 0 order by pvpkills desc limit 2");
			ResultSet rset = statement.executeQuery();
			int rank = 0;
			while (rset.next())
			{
				rank++;
				rewardAmount = 25;
				if (rank == 2)
				{
					rewardAmount = 10;
				}
				
				String name = rset.getString("char_name");
				
				L2PcInstance player = L2World.getInstance().getPlayer(name);
				if (player == null)
				{
					player = L2PcInstance.load(CharNameTable.getInstance().getIdByName(name));
				}
				
				if (player != null)
				{
					player.getInventory().addItem("Event", rewardId, rewardAmount, player, null);
					// MailBBSManager.getInstance().sendMail(player.getName(), "Weekly Event", "" + player.getName() + " you won The Weekly Event - Top Pvp! " + "You've got: " + Util.formatAdena(rewardAmount) + " " + L2Item.getItemNameById(rewardId) + ". Check your inventory!", player, true);
					
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
			PreparedStatement statement = con.prepareStatement("SELECT obj_Id, char_name, pkkills FROM weekly_top_board WHERE pkkills > 0 order by pkkills desc limit 2");
			ResultSet rset = statement.executeQuery();
			int rank = 0;
			while (rset.next())
			{
				rank++;
				rewardAmount = 25;
				if (rank == 2)
				{
					rewardAmount = 10;
				}
				
				String name = rset.getString("char_name");
				
				L2PcInstance player = L2World.getInstance().getPlayer(name);
				if (player == null)
				{
					player = L2PcInstance.load(CharNameTable.getInstance().getIdByName(name));
				}
				
				if (player != null)
				{
					player.getInventory().addItem("Event", rewardId, rewardAmount, player, null);
					// MailBBSManager.getInstance().sendMail(player.getName(), "Weekly Event", "" + player.getName() + " you won The Weekly Event - Top Pk! " + "You've got: " + Util.formatAdena(rewardAmount) + " " + L2Item.getItemNameById(rewardId) + ". Check your inventory!", player, true);
					
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
			LOG.info(getClass().getSimpleName() + ": Couldn't get most pkkills.");
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void getTopClanWarAndAddReward()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT clan_name, SUM(war_points) AS total FROM weekly_top_board WHERE war_points > 0 GROUP BY clan_name ORDER BY total DESC LIMIT 2");
			ResultSet rset = statement.executeQuery();
			int rank = 0;
			while (rset.next())
			{
				rank++;
				rewardAmount = 25;
				if (rank == 2)
				{
					rewardAmount = 10;
				}
				
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
					// MailBBSManager.getInstance().sendMail(player.getName(), "Weekly Event", "" + player.getName() + " you won The Weekly Event - Clan War Points! " + "You've got: " + Util.formatAdena(rewardAmount) + " " + L2Item.getItemNameById(rewardId) + ". Check your inventory!", player,
					// true);
					
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
			LOG.info(getClass().getSimpleName() + ": Couldn't get most war_points.");
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
			PreparedStatement statement = con.prepareStatement("SELECT clan_name, SUM(raid_points) AS total FROM weekly_top_board WHERE raid_points > 0 GROUP BY clan_name ORDER BY total DESC LIMIT 2");
			ResultSet rset = statement.executeQuery();
			int rank = 0;
			while (rset.next())
			{
				rank++;
				rewardAmount = 25;
				if (rank == 2)
				{
					rewardAmount = 10;
				}
				
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
					// MailBBSManager.getInstance().sendMail(player.getName(), "Weekly Event", "" + player.getName() + " you won The Weekly Event - Raid Points! " + "You've got: " + Util.formatAdena(rewardAmount) + " " + L2Item.getItemNameById(rewardId) + ". Check your inventory!", player, true);
					
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
	
	private void getTopFisherAndAddReward()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT obj_Id, char_name, fishing_points FROM weekly_top_board WHERE fishing_points > 0 order by fishing_points desc limit 2");
			ResultSet rset = statement.executeQuery();
			int rank = 0;
			while (rset.next())
			{
				rank++;
				rewardAmount = 25;
				if (rank == 2)
				{
					rewardAmount = 10;
				}
				
				String name = rset.getString("char_name");
				
				L2PcInstance player = L2World.getInstance().getPlayer(name);
				if (player == null)
				{
					player = L2PcInstance.load(CharNameTable.getInstance().getIdByName(name));
					// destChar.deleteMe(); offline char must be disconnected !!! IMPORTANT !!!!
				}
				
				if (player != null)
				{
					player.getInventory().addItem("Event", rewardId, rewardAmount, player, null);
					// MailBBSManager.getInstance().sendMail(player.getName(), "Weekly Event", "" + player.getName() + " you won The Weekly Event - Fishing Contest! " + "You've got: " + Util.formatAdena(rewardAmount) + " " + L2Item.getItemNameById(rewardId) + ". Check your inventory!", player,
					// true);
					
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
			LOG.info(getClass().getSimpleName() + ": Couldn't get most fishing_points.");
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
			ResultSet rset = st.executeQuery("TRUNCATE weekly_top_board");
			rset.close();
			st.close();
		}
		catch (SQLException e)
		{
			LOG.info(getClass().getSimpleName() + ": Couldn't delete weekly_top_board table.");
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		
		if (Config.RON_CUSTOM)
		{
			TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "23:59:00", "");
		}
	}
}