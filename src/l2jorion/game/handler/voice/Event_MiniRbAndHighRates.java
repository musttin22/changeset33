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
package l2jorion.game.handler.voice;

import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.entity.event.custom.HighRatesEvent;
import l2jorion.game.model.entity.event.custom.MiniRbEvent;

public class Event_MiniRbAndHighRates implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"join",
		"leave",
		"eventinfo"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (HighRatesEvent.is_inProgress() || HighRatesEvent.is_joining())
		{
			if (command.startsWith("join"))
			{
				JoinHighRatesEvent(activeChar);
			}
			else if (command.startsWith("leave"))
			{
				LeaveHighRatesEvent(activeChar);
			}
			else if (command.startsWith("eventinfo"))
			{
				HighRatesEventInfo(activeChar);
			}
		}
		else if (MiniRbEvent.is_inProgress() || MiniRbEvent.is_joining())
		{
			if (command.startsWith("join"))
			{
				JoinMiniRbEvent(activeChar);
			}
			else if (command.startsWith("leave"))
			{
				LeaveMiniRbEvent(activeChar);
			}
			else if (command.startsWith("eventinfo"))
			{
				RbEventInfo(activeChar);
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	public boolean JoinMiniRbEvent(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!MiniRbEvent.is_inProgress() && !MiniRbEvent.is_joining())
		{
			activeChar.sendMessage("There is no event in progress.");
			return false;
		}
		else if (MiniRbEvent.is_joining() && activeChar._inEventMiniRb)
		{
			activeChar.sendMessage("You are already registered.");
			return false;
		}
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because you are in Olympiad.");
			return false;
		}
		else
		{
			MiniRbEvent.addPlayer(activeChar);
			MiniRbEvent.showEventHtml(activeChar, String.valueOf(1));
			return true;
		}
	}
	
	public boolean LeaveMiniRbEvent(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!MiniRbEvent.is_inProgress() && !MiniRbEvent.is_joining())
		{
			activeChar.sendMessage("There is no event in progress.");
			return false;
		}
		else if (MiniRbEvent.is_joining() && !activeChar._inEventMiniRb)
		{
			activeChar.sendMessage("You aren't registered in the Event.");
			return false;
		}
		else
		{
			MiniRbEvent.removePlayer(activeChar);
			MiniRbEvent.showEventHtml(activeChar, String.valueOf(1));
			return true;
		}
	}
	
	public boolean RbEventInfo(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		MiniRbEvent.showEventHtml(activeChar, String.valueOf(1));
		return true;
	}
	
	public boolean JoinHighRatesEvent(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!HighRatesEvent.is_inProgress() && !HighRatesEvent.is_joining())
		{
			activeChar.sendMessage("There is no event in progress.");
			return false;
		}
		else if (HighRatesEvent.is_joining() && activeChar._inEventHighRates)
		{
			activeChar.sendMessage("You are already registered.");
			return false;
		}
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because you are in Olympiad.");
			return false;
		}
		else
		{
			HighRatesEvent.addPlayer(activeChar);
			HighRatesEvent.showEventHtml(activeChar, String.valueOf(1));
			return true;
		}
	}
	
	public boolean LeaveHighRatesEvent(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!HighRatesEvent.is_inProgress() && !HighRatesEvent.is_joining())
		{
			activeChar.sendMessage("There is no event in progress.");
			return false;
		}
		else if (HighRatesEvent.is_joining() && !activeChar._inEventHighRates)
		{
			activeChar.sendMessage("You aren't registered in the Event.");
			return false;
		}
		else
		{
			HighRatesEvent.removePlayer(activeChar);
			HighRatesEvent.showEventHtml(activeChar, String.valueOf(1));
			return true;
		}
	}
	
	public boolean HighRatesEventInfo(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		HighRatesEvent.showEventHtml(activeChar, String.valueOf(1));
		return true;
	}
}