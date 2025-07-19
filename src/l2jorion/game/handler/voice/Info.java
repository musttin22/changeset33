package l2jorion.game.handler.voice;

import l2jorion.game.cache.HtmCache;
import l2jorion.game.handler.IVoicedCommandHandler;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.serverpackets.NpcHtmlMessage;

public class Info implements IVoicedCommandHandler
{
	private static String[] _voicedCommands =
	{
		"info"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String parameters)
	{
		if (command.equalsIgnoreCase("info"))
		{
			showCommand(activeChar);
			return true;
		}
		return false;
	}
	
	private void showCommand(L2PcInstance player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(player.getLastQuestNpcObject());
		String text = HtmCache.getInstance().getHtm("data/html/mods/info.htm");
		htm.setHtml(text);
		player.sendPacket(htm);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
