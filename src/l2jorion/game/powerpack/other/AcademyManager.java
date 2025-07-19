package l2jorion.game.powerpack.other;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2jorion.game.datatables.sql.ClanTable;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.idfactory.IdFactory;
import l2jorion.game.model.L2Clan;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.ItemList;
import l2jorion.game.network.serverpackets.JoinPledge;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.game.network.serverpackets.PledgeShowInfoUpdate;
import l2jorion.game.network.serverpackets.PledgeShowMemberListAdd;
import l2jorion.game.network.serverpackets.PledgeShowMemberListAll;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.util.Util;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.DatabaseUtils;
import l2jorion.util.database.L2DatabaseFactory;

public class AcademyManager implements ICustomByPassHandler
{
	protected static final Logger LOG = LoggerFactory.getLogger(AcademyManager.class);
	
	private boolean _playerAlreadyAdded = false;
	
	@Override
	public String[] getByPassCommands()
	{
		return new String[]
		{
			"manager_index",
			"manager_join",
			"manager_menu_add",
			"manager_add",
			"manager_cancel"
		};
	}
	
	private enum CommandEnum
	{
		manager_index,
		manager_join,
		manager_menu_add,
		manager_add,
		manager_cancel
	}
	
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		CommandEnum comm = CommandEnum.valueOf(command);
		if (comm == null)
		{
			return;
		}
		
		StringTokenizer st = new StringTokenizer(parameters, " ");
		
		switch (comm)
		{
			case manager_index:
			{
				showMainWindow(player);
				break;
			}
			case manager_join:
			{
				joinAcademy(player, Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
				break;
			}
			case manager_menu_add:
			{
				showAddMenu(player);
				break;
			}
			case manager_add:
			{
				if (player.getClan() == null)
				{
					player.sendMessage("You don't have a clan.");
					player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
					showAddMenu(player);
					return;
				}
				
				if (player.getClan().getLeaderId() != player.getObjectId())
				{
					player.sendMessage("You are not a clan leader.");
					player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
					showAddMenu(player);
					return;
				}
				
				if (player.getClan().getSubPledge(-1) == null)
				{
					player.sendMessage("You didn't create an Academy yet.");
					player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
					showAddMenu(player);
					return;
				}
				
				String slot = st.nextToken();
				String count = st.nextToken();
				String itemName = st.nextToken();
				
				long members = Long.parseLong(slot);
				long reward_count = Long.parseLong(count);
				int itemId = 6393;
				
				if (itemName.startsWith("Adena"))
				{
					itemId = 57;
				}
				
				if (members < 1)
				{
					members = 1;
				}
				
				L2ItemInstance currency = player.getInventory().getItemByItemId(itemId);
				
				if ((reward_count * members) >= Integer.MAX_VALUE)
				{
					player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
					player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
					showAddMenu(player);
					return;
				}
				
				if (currency == null || currency.getCount() < (reward_count * members))
				{
					player.sendMessage("You don't have enough items.");
					player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
					showAddMenu(player);
					return;
				}
				
				player.destroyItem("Consume", currency.getObjectId(), ((int) reward_count * (int) members), null, true);
				
				storeOrUpdate(player, (int) members, (int) reward_count, itemId);
				player.sendMessage("The Reward has been successfully added.");
				showAddMenu(player);
				break;
			}
			case manager_cancel:
			{
				if (player.getClan() != null && player.getClan().getLeaderId() == player.getObjectId())
				{
					deleteDataAndAddItemsBack(player, player.getClanId());
					showAddMenu(player);
				}
				break;
			}
		}
	}
	
	public static void showMainWindow(L2PcInstance player)
	{
		int color = 1;
		String clanName = "";
		String filename = "data/html/mods/academyManager/manager.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(4);
		html.setFile(filename);
		TextBuilder reply = new TextBuilder();
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM academyReward WHERE reward_count > 0 ORDER BY reward_count DESC");
			
			final ResultSet data = statement.executeQuery();
			
			while (data.next())
			{
				if (color == 1)
				{
					reply.append("<table width=\"322\" border=\"0\">");
					color = 2;
				}
				else
				{
					reply.append("<table width=\"322\" bgcolor=\"000000\" border=\"0\">");
					color = 1;
				}
				
				clanName = data.getString("clan_name");
				
				reply.append("<tr>");
				reply.append("<td width=\"100\"> " + clanName + "</td>");
				reply.append("<td width=\"162\"><font color=\"LEVEL\">" + Util.formatAdena(data.getInt("reward_count")) + "</font> " + (data.getInt("reward_id") == 57 ? " Adena" : " Glittering Medals") + "</td>");
				reply.append("<td width=\"40\"><a action=\"bypass -h custom_manager_join " + data.getInt("reward_count") + " " + data.getInt("reward_id") + "  " + data.getInt("slot") + " " + data.getInt("clan_id") + "\" msg=\"Join to The Academy of " + clanName + "\">[+]</a></td>");
				reply.append("</tr>");
				reply.append("</table>");
			}
			
			data.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOG.error("Error restoring clan academyReward: " + e.getMessage(), e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		html.replace("%list%", reply.toString());
		player.sendPacket(html);
	}
	
	private void showAddMenu(L2PcInstance player)
	{
		if (player.getClan() == null)
		{
			player.sendMessage("You don't have a clan.");
			player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
			showMainWindow(player);
			return;
		}
		
		if (player.getClan().getLeaderId() != player.getObjectId())
		{
			player.sendMessage("You are not a clan leader.");
			player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
			showMainWindow(player);
			return;
		}
		
		int color = 1;
		
		_playerAlreadyAdded = false;
		
		TextBuilder reply = new TextBuilder();
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT clan_id, clan_name, slot, reward_id, reward_count FROM academyReward WHERE reward_count > 0 ORDER BY reward_count DESC");
			
			final ResultSet data = statement.executeQuery();
			
			while (data.next())
			{
				if (player.getClan() != null)
				{
					if (player.getClanId() == data.getInt("clan_id"))
					{
						_playerAlreadyAdded = true;
						
						if (color == 1)
						{
							reply.append("<table width=\"322\" border=\"0\">");
							color = 2;
						}
						else
						{
							reply.append("<table width=\"322\" bgcolor=\"000000\" border=\"0\">");
							color = 1;
						}
						
						reply.append("<tr>");
						reply.append("<td width=\"162\"><font color=\"LEVEL\">" + Util.formatAdena(data.getInt("reward_count")) + "</font> " + (data.getInt("reward_id") == 57 ? " Adena" : " Glittering Medals") + "</td>");
						reply.append("<td width=\"100\"> " + data.getInt("slot") + "</td>");
						reply.append("<td width=\"60\"><a action=\"bypass -h custom_manager_cancel\">[-]</a></td>");
						reply.append("</tr>");
						reply.append("</table>");
					}
				}
				
			}
			
			data.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOG.error("Error restoring clan academyReward: " + e.getMessage(), e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		String filename = "data/html/mods/academyManager/addMenu.htm";
		
		if (_playerAlreadyAdded)
		{
			filename = "data/html/mods/academyManager/addMenuDone.htm";
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(4);
		html.setFile(filename);
		
		html.replace("%myList%", reply.toString());
		player.sendPacket(html);
	}
	
	// XXX joinAcademy
	private void joinAcademy(L2PcInstance activeChar, int rewardCount, int rewardId, int slot, int clanId)
	{
		final L2Clan clan = ClanTable.getInstance().getClan(clanId);
		int updatedSlot = (slot - 1);
		
		if (clan != null && clan.checkAcademyJoinCondition(activeChar, -1))
		{
			final JoinPledge jp = new JoinPledge(clanId);
			activeChar.sendPacket(jp);
			
			activeChar.setPledgeType(-1);
			
			activeChar.setPowerGrade(9); // adademy
			activeChar.setLvlJoinedAcademy(activeChar.getLevel());
			
			clan.addClanMember(activeChar);
			activeChar.setClanPrivileges(activeChar.getClan().getRankPrivs(activeChar.getPowerGrade()));
			
			activeChar.sendPacket(new SystemMessage(SystemMessageId.ENTERED_THE_CLAN));
			
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN);
			sm.addString(activeChar.getName());
			clan.broadcastToOnlineMembers(sm);
			
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(activeChar), activeChar);
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
			
			// this activates the clan tab on the new member
			activeChar.sendPacket(new PledgeShowMemberListAll(clan, activeChar));
			activeChar.setClanJoinExpiryTime(0);
			activeChar.broadcastUserInfo();
			
			insertOrUpdateNewMember(activeChar, rewardCount, rewardId);
			
			if (updatedSlot <= 0)
			{
				DatabaseUtils.set("DELETE FROM academyReward WHERE clan_id=?", clanId);
				return;
			}
			
			updateClanSlot(activeChar, updatedSlot, clanId);
		}
	}
	
	private void storeOrUpdate(L2PcInstance player, int slot, int rewardCount, int rewardId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("INSERT INTO academyReward (clan_id, clan_name, slot, reward_id, reward_count) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE clan_name=?, slot=?, reward_id=?, reward_count=?");
			
			statement.setInt(1, player.getClan().getClanId());
			
			statement.setString(2, player.getClan().getName());
			statement.setInt(3, slot);
			statement.setInt(4, rewardId);
			statement.setInt(5, rewardCount);
			
			statement.setString(6, player.getClan().getName());
			statement.setInt(7, slot);
			statement.setInt(8, rewardId);
			statement.setInt(9, rewardCount);
			
			statement.execute();
			statement.close();
		}
		catch (final Exception e)
		{
			LOG.warn("Error could not store storeOrUpdate: " + e.getMessage(), e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void deleteDataAndAddItemsBack(L2PcInstance player, int clan_id)
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			Statement st = con.createStatement();
			ResultSet rset = st.executeQuery("SELECT * FROM academyReward");
			
			while (rset.next())
			{
				int clanId = rset.getInt("clan_id");
				
				if (player != null && player.getClan().getClanId() == clanId)
				{
					int slot = rset.getInt("slot");
					int itemId = rset.getInt("reward_id");
					int itemCount = rset.getInt("reward_count");
					
					addItemBack(player, itemId, itemCount * slot);
					
					DatabaseUtils.set("DELETE FROM academyReward WHERE clan_id=?", clan_id);
				}
			}
			
			rset.close();
			st.close();
		}
		catch (SQLException e)
		{
			LOG.warn(getClass().getSimpleName() + ": Couldn't add items back:" + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public static void deleteDataAndAddReward(L2PcInstance player)
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			Statement st = con.createStatement();
			ResultSet rset = st.executeQuery("SELECT * FROM academyMembers");
			
			while (rset.next())
			{
				int playerId = rset.getInt("player_id");
				
				if (player != null && player.getObjectId() == playerId)
				{
					int itemId = rset.getInt("reward_id");
					int itemCount = rset.getInt("reward_count");
					
					addItemBack(player, itemId, itemCount);
					
					DatabaseUtils.set("DELETE FROM academyMembers WHERE player_id=?", playerId);
				}
			}
			
			rset.close();
			st.close();
		}
		catch (SQLException e)
		{
			LOG.warn("deleteDataAndAddReward: Couldn't add items back:" + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public static void deleteDataAndAddItemsBackForCL(L2PcInstance leader, int leaderId, int memberId)
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			Statement st = con.createStatement();
			ResultSet rset = st.executeQuery("SELECT * FROM academyMembers");
			
			while (rset.next())
			{
				int playerId = rset.getInt("player_id");
				
				if (memberId == playerId)
				{
					int itemId = rset.getInt("reward_id");
					int itemCount = rset.getInt("reward_count");
					
					if (leader != null && leader.isOnline() == 1)
					{
						addItemBack(leader, itemId, itemCount);
					}
					else
					{
						L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
						addItemForOffliner(leaderId, item.getObjectId(), itemId, itemCount);
					}
					
					DatabaseUtils.set("DELETE FROM academyMembers WHERE player_id=?", playerId);
				}
			}
			
			rset.close();
			st.close();
		}
		catch (SQLException e)
		{
			LOG.warn("deleteDataAndAddReward: Couldn't add items back:" + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public static void addItemForOffliner(int ownerId, int objectId, int itemId, int itemCount)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("INSERT INTO items Values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
			
			statement.setInt(1, ownerId);
			statement.setInt(2, objectId);
			statement.setInt(3, itemId);
			statement.setInt(4, itemCount);
			statement.setInt(5, 0);// enchant level
			statement.setString(6, "INVENTORY");
			statement.setInt(7, 0);
			statement.setInt(8, 0);
			statement.setInt(9, 0);
			statement.setInt(10, 0);
			statement.setInt(11, 0);
			statement.setInt(12, 0);
			statement.setInt(13, -1);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOG.warn("Error while adding a new time into DB: " + e.getMessage());
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void updateClanSlot(L2PcInstance player, int slot, int clan_id)
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("UPDATE academyReward Set slot = ? WHERE clan_id = ?");
			statement.setInt(1, slot);
			statement.setInt(2, clan_id);
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			LOG.warn(getClass().getSimpleName() + ": Couldn't updateClanSlot:" + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void insertOrUpdateNewMember(L2PcInstance player, int rewardCount, int rewardId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("INSERT INTO academyMembers (player_id, reward_id, reward_count) values (?,?,?) ON DUPLICATE KEY UPDATE reward_id=?, reward_count=?");
			
			statement.setInt(1, player.getObjectId());
			
			statement.setInt(2, rewardId);
			statement.setInt(3, rewardCount);
			
			statement.setInt(4, rewardId);
			statement.setInt(5, rewardCount);
			
			statement.execute();
			statement.close();
		}
		catch (final Exception e)
		{
			LOG.error("Error could not store insertOrUpdateNewMember: " + e.getMessage(), e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private static void addItemBack(L2PcInstance target, int itemId, int itemCount)
	{
		L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setCount(itemCount);
		
		target.getInventory().addItem("AddItemBack", item, target, null);
		
		target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addNumber(itemCount));
		target.sendPacket(new ItemList(target, true));
	}
}