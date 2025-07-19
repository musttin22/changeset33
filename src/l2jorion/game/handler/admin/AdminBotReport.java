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
package l2jorion.game.handler.admin;

import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javolution.text.TextBuilder;
import l2jorion.game.handler.IAdminCommandHandler;
import l2jorion.game.managers.BotsManager;
import l2jorion.game.managers.BotsManager.BotDataHolder;
import l2jorion.game.model.L2World;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;

public class AdminBotReport implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_botreport",
		"admin_botreport_delete"
	};
	
	private enum CommandEnum
	{
		admin_botreport,
		admin_botreport_delete
	}
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		
		CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_botreport:
			{
				String page = "1";
				
				if (st.hasMoreTokens())
				{
					page = st.nextToken();
				}
				
				handleMenu(activeChar, Integer.parseInt(page));
				break;
			}
			case admin_botreport_delete:
			{
				String page = "1";
				String objectId = "0";
				String botName = "";
				
				if (st.hasMoreTokens())
				{
					page = st.nextToken();
					objectId = st.nextToken();
					botName = st.nextToken();
				}
				
				if (Integer.parseInt(objectId) > 0)
				{
					if (BotsManager.getInstance().getBotsList().containsKey(Integer.parseInt(objectId)))
					{
						BotsManager.getInstance().getBotsList().remove(Integer.parseInt(objectId));
						activeChar.sendMessage(botName + " removed from the list.");
						activeChar.sendPacket(new ExShowScreenMessage(botName + " removed from the list.", 2000, 2, false));
					}
				}
				
				handleMenu(activeChar, Integer.parseInt(page));
				break;
			}
		}
		
		return false;
	}
	
	private void handleMenu(final L2PcInstance player, int page)
	{
		int pageLimit = 15;
		int count = 0;
		int color = 1;
		String status = "";
		L2PcInstance target = null;
		
		List<BotDataHolder> list = BotsManager.getInstance().getBotsList().values().stream().sorted(Comparator.comparingLong(bot -> ((BotDataHolder) bot).getReportCount()).reversed()).collect(Collectors.toList());
		
		NpcHtmlMessage npcReply = new NpcHtmlMessage(1);
		TextBuilder reply = new TextBuilder("<html><head><title>Bots List: " + (list.isEmpty() ? "0" : list.size()) + "</title></head><body>");
		
		reply.append("<img src=\"l2ui.SquareBlack\" width=\"298\" height=\"1\">");
		reply.append("<table width=300 height=20><tr><td width=20 align=right>#</td><td width=120>Name</td><td width=80>Reports</td><td width=40>Status</td><td width=40>Delete</td></tr></table>");
		reply.append("<img src=\"l2ui.SquareBlack\" width=\"298\" height=\"1\">");
		
		if (!list.isEmpty())
		{
			final int max = list.size() / pageLimit + (list.size() % pageLimit == 0 ? 0 : 1);
			page = page > max ? max : page < 1 ? 1 : page;
			list = list.subList((page - 1) * pageLimit, Math.min(page * pageLimit, list.size()));
			
			for (BotDataHolder bot : list)
			{
				if (bot == null)
				{
					continue;
				}
				
				count++;
				
				target = L2World.getInstance().getPlayer(bot.getBotName());
				
				if (target != null && target.isOnline() == 1)
				{
					status = "<img src=\"panels.on\" width=\"16\" height=\"16\">";
				}
				else
				{
					status = "<img src=\"panels.off\" width=\"16\" height=\"16\">";
				}
				
				if (color == 1)
				{
					reply.append("<table width=300 border=0 bgcolor=000000><tr>");
					color = 2;
				}
				else
				{
					reply.append("<table width=300 border=0><tr>");
					color = 1;
				}
				
				reply.append("<td width=20 align=right>" + count + ".</td><td width=120><a action=\"bypass admin_character_info " + bot.getBotName() + "\">" + bot.getBotName() + " </a></td>"//
					+ "<td width=80>" + bot.getReportCount() + "</td><td width=40>" + status + "</td><td width=40><a action=\"bypass admin_botreport_delete " + page + " " + bot.getBotId() + " " + bot.getBotName() + "\">[X]</a></td>");
				
				reply.append("</tr></table>");
				reply.append("<img src=\"l2ui.SquareBlack\" width=\"298\" height=\"1\">");
			}
			
			reply.append("<table width=300><tr>");
			reply.append("<td width=66>" + ((page == 1) ? "" : "<button value=\"Back\" action=\"bypass -h admin_botreport " + (page - 1) + "\"width=66 height=16 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">") + "</td>");
			reply.append("<td align=center width=138><img height=-3>Page: " + page + " / " + max + "</td>");
			reply.append("<td width=66>" + ((page < max) ? "<button value=\"Next\" action=\"bypass admin_botreport " + (page + 1) + "\" width=66 height=16 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">" : "") + "</td>");
			reply.append("</tr></table>");
		}
		else
		{
			reply.append("<br><center>List is empty</center>");
			reply.append("<img src=\"l2ui.SquareBlack\" width=\"298\" height=\"1\">");
		}
		
		reply.append("<center><img height=-2><button value=\"Update\" action=\"bypass admin_botreport\" width=66 height=16 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\"></center>");
		reply.append("</body></html>");
		npcReply.setHtml(reply.toString());
		player.sendPacket(npcReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
