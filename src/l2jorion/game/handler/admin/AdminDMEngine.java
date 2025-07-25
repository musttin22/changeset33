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
package l2jorion.game.handler.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import javolution.text.TextBuilder;
import l2jorion.game.handler.IAdminCommandHandler;
import l2jorion.game.model.Location;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.event.DM;
import l2jorion.game.model.entity.event.DMSpawnLocations;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;
import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;
import l2jorion.util.CloseUtil;
import l2jorion.util.database.L2DatabaseFactory;

public class AdminDMEngine implements IAdminCommandHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(AdminDMEngine.class);
	// XXX top
	private static final String GET_LOCS = "SELECT * FROM `dm_spawn_locations` ORDER BY `date` DESC";
	private static final String DELETE_LOC = "DELETE FROM `dm_spawn_locations` WHERE `locId`=?";
	private static final String ADD_LOC = "REPLACE INTO `dm_spawn_locations`(`location`) VALUES(?)";
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_dmevent",
		"admin_dmevent_name",
		"admin_dmevent_desc",
		"admin_dmevent_join_loc",
		"admin_dmevent_minlvl",
		"admin_dmevent_maxlvl",
		"admin_dmevent_npc",
		"admin_dmevent_npc_pos",
		"admin_dmevent_reward",
		"admin_dmevent_reward_amount",
		"admin_dmevent_spawnpos",
		"admin_dmevent_color",
		"admin_dmevent_join",
		"admin_dmevent_teleport",
		"admin_dmevent_start",
		"admin_dmevent_startevent",
		"admin_dmevent_abort",
		"admin_dmevent_finish",
		"admin_dmevent_sit",
		"admin_dmevent_dump",
		"admin_dmevent_save",
		"admin_dmevent_load",
		"admin_dmevent_reload_spawn_points",
		"admin_dmevent_show_add_loc_page",
		"admin_dmevent_add_loc",
		"admin_dmevent_delete_loc"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.equals("admin_dmevent"))
		{
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_dmevent_name "))
		{
			if (DM.set_eventName(command.substring(19)))
			{
				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Cannot perform requested operation, event in progress");
			}
			
		}
		
		else if (command.startsWith("admin_dmevent_desc "))
		{
			if (DM.set_eventDesc(command.substring(19)))
			{
				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Cannot perform requested operation, event in progress");
			}
			
		}
		
		else if (command.startsWith("admin_dmevent_minlvl "))
		{
			if (!DM.checkMinLevel(Integer.valueOf(command.substring(21))))
			{
				return false;
			}
			
			if (DM.set_minlvl(Integer.valueOf(command.substring(21))))
			{
				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Cannot perform requested operation, event in progress");
			}
			
		}
		
		else if (command.startsWith("admin_dmevent_maxlvl "))
		{
			if (!DM.checkMaxLevel(Integer.valueOf(command.substring(21))))
			{
				return false;
			}
			
			if (DM.set_maxlvl(Integer.valueOf(command.substring(21))))
			{
				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Cannot perform requested operation, event in progress");
			}
			
		}
		
		else if (command.startsWith("admin_dmevent_join_loc "))
		{
			if (DM.set_joiningLocationName(command.substring(23)))
			{
				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Cannot perform requested operation, event in progress");
			}
			
		}
		
		else if (command.startsWith("admin_dmevent_npc "))
		{
			if (DM.set_npcId(Integer.valueOf(command.substring(18))))
			{
				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Cannot perform requested operation, event in progress");
			}
			
		}
		
		else if (command.equals("admin_dmevent_npc_pos"))
		{
			DM.setNpcPos(activeChar);
			showMainPage(activeChar);
		}
		
		else if (command.startsWith("admin_dmevent_reward "))
		{
			if (DM.set_rewardId(Integer.valueOf(command.substring(21))))
			{
				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Cannot perform requested operation, event in progress");
			}
			
		}
		
		else if (command.startsWith("admin_dmevent_reward_amount "))
		{
			if (DM.set_rewardAmount(Integer.valueOf(command.substring(28))))
			{
				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Cannot perform requested operation, event in progress");
			}
			
		}
		
		else if (command.equals("admin_dmevent_spawnpos"))
		{
			DM.setPlayersPos(activeChar);
			showMainPage(activeChar);
		}
		
		else if (command.startsWith("admin_dmevent_color "))
		{
			if (DM.set_playerColors(Integer.decode("0x" + command.substring(20))))
			{
				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Cannot perform requested operation, event in progress");
			}
			
		}
		
		else if (command.equals("admin_dmevent_join"))
		{
			if (DM.startJoin())
			{
				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Cannot startJoin, check LOGGER for info..");
			}
		}
		
		else if (command.equals("admin_dmevent_teleport"))
		{
			DM.startTeleport();
			showMainPage(activeChar);
		}
		
		else if (command.equals("admin_dmevent_start"))
		{
			if (DM.startEvent())
			{
				showMainPage(activeChar);
			}
			else
			{
				activeChar.sendMessage("Cannot startEvent, check LOGGER for info..");
			}
			
		}
		
		else if (command.equals("admin_dmevent_startevent"))
		{
			DM.startEvent();
			showMainPage(activeChar);
			
		}
		
		else if (command.equals("admin_dmevent_abort"))
		{
			activeChar.sendMessage("Aborting event");
			DM.abortEvent();
			showMainPage(activeChar);
		}
		
		else if (command.equals("admin_dmevent_finish"))
		{
			DM.finishEvent();
			showMainPage(activeChar);
		}
		
		else if (command.equals("admin_dmevent_sit"))
		{
			DM.sit();
			showMainPage(activeChar);
		}
		
		else if (command.equals("admin_dmevent_load"))
		{
			DM.loadData();
			showMainPage(activeChar);
		}
		
		else if (command.equals("admin_dmevent_save"))
		{
			DM.saveData();
			showMainPage(activeChar);
		}
		
		else if (command.equals("admin_dmevent_dump"))
		{
			DM.dumpData();
		}
		else if (command.equals("admin_dmevent_reload_spawn_points"))
		{
			DMSpawnLocations.getInstance().reload();
			activeChar.sendMessage("DM Event: spawn locatios reloaded.");
			showAddLocPage(activeChar);
			return true;
		}
		if (command.startsWith("admin_dmevent_show_add_loc_page"))
		{
			showAddLocPage(activeChar);
			return true;
		}
		else if (command.startsWith("admin_dmevent_add_loc"))
		{
			String loc = activeChar.getX() + "," + activeChar.getY() + "," + activeChar.getZ();
			
			Connection con = null;
			
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(ADD_LOC);
				
				ps.setString(1, loc);
				ps.execute();
				
				DMSpawnLocations.getInstance().getAllSpawnLocations().add(new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()));
				
				showAddLocPage(activeChar);
			}
			catch (Exception e)
			{
			}
			finally
			{
				CloseUtil.close(con);
			}
		}
		else if (command.startsWith("admin_dmevent_delete_loc"))
		{
			final String[] parts = command.split(" ");
			if (parts.length > 3)
			{
				parts[2] = "" + parts[2] + " " + parts[3] + "";
			}
			
			Connection con = null;
			
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(DELETE_LOC);
				
				ps.setInt(1, Integer.parseInt(parts[1]));
				ps.execute();
				
				// DMSpawnLocations.getInstance().getAllSpawnLocations().remove(Integer.parseInt(parts[1]));
				
				showAddLocPage(activeChar);
			}
			catch (Exception e)
			{
				LOG.warn("" + e);
			}
			finally
			{
				CloseUtil.close(con);
			}
		}
		
		return true;
	}
	
	// XXX bottom
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	public void showMainPage(final L2PcInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		
		replyMSG.append("<center><font color=\"LEVEL\">[DeathMatch Engine]</font></center><br><br>");
		
		replyMSG.append("<center><button value=\"Add/Edit respawn\" action=\"bypass -h admin_dmevent_show_add_loc_page\" width=90 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></center>");
		
		replyMSG.append("<table><tr><td><edit var=\"input1\" width=\"125\"></td><td><edit var=\"input2\" width=\"125\"></td></tr></table>");
		replyMSG.append("<table border=\"0\"><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Name\" action=\"bypass -h admin_dmevent_name $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Description\" action=\"bypass -h admin_dmevent_desc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Join Location\" action=\"bypass -h admin_dmevent_join_loc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Max lvl\" action=\"bypass -h admin_dmevent_maxlvl $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Min lvl\" action=\"bypass -h admin_dmevent_minlvl $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"NPC\" action=\"bypass -h admin_dmevent_npc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"NPC Pos\" action=\"bypass -h admin_dmevent_npc_pos\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Reward\" action=\"bypass -h admin_dmevent_reward $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Reward Amount\" action=\"bypass -h admin_dmevent_reward_amount $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"DM Color\" action=\"bypass -h admin_dmevent_color $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"DM SpawnPos\" action=\"bypass -h admin_dmevent_spawnpos\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><table><br><br><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Join\" action=\"bypass -h admin_dmevent_join\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Teleport\" action=\"bypass -h admin_dmevent_teleport\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Start\" action=\"bypass -h admin_dmevent_start\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><table><tr>");
		replyMSG.append("</tr></table><table><br><br><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"StartEventOnceTime\" action=\"bypass -h admin_dmevent_startevent\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Abort\" action=\"bypass -h admin_dmevent_abort\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Finish\" action=\"bypass -h admin_dmevent_finish\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Sit Force\" action=\"bypass -h admin_dmevent_sit\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Dump\" action=\"bypass -h admin_dmevent_dump\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Save\" action=\"bypass -h admin_dmevent_save\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Load\" action=\"bypass -h admin_dmevent_load\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><br>");
		replyMSG.append("Current event...<br1>");
		replyMSG.append("Name:&nbsp;<font color=\"00FF00\">" + DM.get_eventName() + "</font><br1>");
		replyMSG.append("Description:&nbsp;<font color=\"00FF00\">" + DM.get_eventDesc() + "</font><br1>");
		replyMSG.append("Joining location name:&nbsp;<font color=\"00FF00\">" + DM.get_joiningLocationName() + "</font><br1>");
		
		final Location npc_loc = DM.get_npcLocation();
		
		replyMSG.append("Joining NPC ID:&nbsp;<font color=\"00FF00\">" + DM.get_npcId() + " on pos " + npc_loc._x + "," + npc_loc._y + "," + npc_loc._z + "</font><br1>");
		replyMSG.append("Reward ID:&nbsp;<font color=\"00FF00\">" + DM.get_rewardId() + "</font><br1>");
		replyMSG.append("Reward Amount:&nbsp;<font color=\"00FF00\">" + DM.get_rewardAmount() + "</font><br><br>");
		replyMSG.append("Min lvl:&nbsp;<font color=\"00FF00\">" + DM.get_minlvl() + "</font><br>");
		replyMSG.append("Max lvl:&nbsp;<font color=\"00FF00\">" + DM.get_maxlvl() + "</font><br><br>");
		replyMSG.append("Death Match Color:&nbsp;<font color=\"00FF00\">" + DM.get_playerColors() + "</font><br>");
		
		final Location player_loc = DM.get_playersSpawnLocation();
		
		replyMSG.append("Death Match Spawn Pos:&nbsp;<font color=\"00FF00\">" + player_loc._x + "," + player_loc._y + "," + player_loc._z + "</font><br><br>");
		replyMSG.append("Current players:<br1>");
		
		if (!DM.is_started())
		{
			replyMSG.append("<br1>");
			replyMSG.append(DM._players.size() + " players participating.");
			replyMSG.append("<br><br>");
		}
		
		else if (DM.is_started())
		{
			replyMSG.append("<br1>");
			replyMSG.append(DM._players.size() + " players in fighting event.");
			replyMSG.append("<br><br>");
		}
		
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	// XXX showAddLocPage
	public void showAddLocPage(final L2PcInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(1);
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(GET_LOCS);
			
			final TextBuilder replyMSG = new TextBuilder("<html><body>");
			replyMSG.append("<center><font color=\"LEVEL\">[DeathMatch Engine]</font></center><br>");
			replyMSG.append("<center><button value=\"Main menu\" action=\"bypass -h admin_dmevent\" width=90 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1></center>");
			replyMSG.append("<center><button value=\"Reload respawn locs\" action=\"bypass -h admin_dmevent_reload_spawn_points\" width=90 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></center>");
			
			replyMSG.append("<center><font color=009900>(Your character's location = new respawn location)</font></center>");
			replyMSG.append("<center><button value=\"Add respawn loc\" action=\"bypass -h admin_dmevent_add_loc\" width=90 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1></center>");
			
			replyMSG.append("<table border=0 cellspacing=0 cellpadding=0 width=300 height=20 bgcolor=111111>");
			replyMSG.append("<tr><td width=20>#</td>");
			replyMSG.append("<td width=100 align=left>Location</td>");
			replyMSG.append("<td width=50 align=left>Added</td>");
			replyMSG.append("<td width=30 align=center>Del</td></tr>");
			replyMSG.append("</table>");
			
			replyMSG.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\" width=\"300\">");
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					int locId = rs.getInt("locId");
					String location = rs.getString("location");
					final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String timeDate = date.format(rs.getTimestamp("date"));
					String[] coord = location.split(",");
					
					replyMSG.append("<tr>");
					replyMSG.append("<td width=\"20\">" + locId + ".</td>");
					replyMSG.append("<td width=\"100\" align=\"left\"><a action=\"bypass -h admin_move_to " + coord[0] + " " + coord[1] + " " + coord[2] + "\" msg=\"811;" + locId + ". " + location + "\">" + location + "</a></td>");
					replyMSG.append("<td width=\"50\" align=\"center\">" + timeDate + "</td>");
					replyMSG.append("<td width=\"30\" align=\"center\"><a action=\"bypass -h admin_dmevent_delete_loc " + locId + "\">[X]</a></td>");
					replyMSG.append("</tr>");
				}
			}
			replyMSG.append("</table>");
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		catch (Exception e)
		{
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
}