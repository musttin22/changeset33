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

import l2jorion.Config;
import l2jorion.game.handler.IAdminCommandHandler;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;

public class AdminHelpPage implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_help"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_help"))
		{
			try
			{
				String val = command.substring(11);
				showHelpPage(activeChar, val);
				val = null;
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	public static void showHelpPage(final L2PcInstance targetChar, final String filename)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/" + filename);
		targetChar.sendPacket(adminReply);
	}
	
	public static void showSubMenuPage(final L2PcInstance targetChar, final String filename)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/" + filename);
		targetChar.sendPacket(adminReply);
	}
}
