package l2jorion.game.handler;

import l2jorion.game.controllers.InertiaController;
import l2jorion.game.model.actor.instance.L2PcInstance;

/**
 * Handler para comandos de voz de Inertia adaptado para L2JOrion changeset 33 rev 1161
 */
public class InertiaVoicedCommandHandler implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"inertia",
		"bot",
		"auto"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (command.equals("inertia") || command.equals("bot") || command.equals("auto"))
		{
			if (target == null || target.isEmpty())
			{
				InertiaController.showPanel(activeChar);
			}
			else
			{
				InertiaController.processCommand(activeChar, target);
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}