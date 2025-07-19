package l2jorion.game.handler.voice;

import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.managers.BotsManager;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.PlaySound;

public class botReport implements IVoicedCommandHandler
{
	private static String[] _voicedCommands =
	{
		"botreport"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String parameters)
	{
		if (command.startsWith("botreport"))
		{
			if (activeChar.getTarget() == null)
			{
				activeChar.sendMessage("No target! Choose your target and write: .botreport");
				activeChar.sendPacket(new ExShowScreenMessage("No target! Choose your target and write: .botreport", 2000, 2, false));
				activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return false;
			}
			
			if (BotsManager.getInstance().getPlayerReports(activeChar) >= 5)
			{
				activeChar.sendMessage("You have reached your daily limit: " + BotsManager.getInstance().getPlayerReports(activeChar) + "/5");
				activeChar.sendPacket(new ExShowScreenMessage("You have reached your daily limit: " + BotsManager.getInstance().getPlayerReports(activeChar) + "/5", 2000, 2, false));
				activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return false;
			}
			
			if (!activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage("Your target is not player.");
				activeChar.sendPacket(new ExShowScreenMessage("Your target is not player.", 2000, 2, false));
				activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return false;
			}
			
			if (activeChar.getTarget() == activeChar)
			{
				activeChar.sendMessage("You can't report your own character.");
				activeChar.sendPacket(new ExShowScreenMessage("You can't report your own character.", 2000, 2, false));
				activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return false;
			}
			
			if (BotsManager.getInstance().isAlreadyRepored(activeChar, (L2PcInstance) activeChar.getTarget()))
			{
				activeChar.sendMessage("You already reported this player.");
				activeChar.sendPacket(new ExShowScreenMessage("You already reported this player.", 2000, 2, false));
				activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				return false;
			}
			
			BotsManager.getInstance().addReporterToList(activeChar, (L2PcInstance) activeChar.getTarget());
			BotsManager.getInstance().addBotToList((L2PcInstance) activeChar.getTarget());
			
			activeChar.sendMessage("Thank you! Player has been added to the list successfully. " + BotsManager.getInstance().getPlayerReports(activeChar) + "/5");
			activeChar.sendPacket(new ExShowScreenMessage("Thank you! Player has been added to the list successfully. " + BotsManager.getInstance().getPlayerReports(activeChar) + "/5", 2000, 2, false));
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
