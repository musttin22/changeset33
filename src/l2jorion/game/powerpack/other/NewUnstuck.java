package l2jorion.game.powerpack.other;

import l2jorion.game.datatables.xml.MapRegionTable;
import l2jorion.game.handler.ICustomByPassHandler;
import l2jorion.game.managers.CastleManager;
import l2jorion.game.managers.ClanHallManager;
import l2jorion.game.model.actor.instance.L2PcInstance;

public class NewUnstuck implements ICustomByPassHandler
{
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if (parameters.startsWith("do_teleport"))
		{
			final int index = Integer.parseInt(parameters.substring(11).trim());
			
			switch (index)
			{
				case 1: // town
				{
					player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
					break;
				}
				case 2: // castle
				{
					if (CastleManager.getInstance().getCastleByOwner(player.getClan()) != null)
					{
						player.teleToLocation(MapRegionTable.TeleportWhereType.Castle);
					}
					else
					{
						player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
					}
					break;
				}
				case 3: // clanhall
				{
					if (player.getClan() != null && ClanHallManager.getInstance().getClanHallByOwner(player.getClan()) != null) // escape to clan hall if own's one
					{
						player.teleToLocation(MapRegionTable.TeleportWhereType.ClanHall);
					}
					else
					{
						player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
					}
					break;
				}
				default:
					break;
			}
		}
	}
	
	private static String[] _newunstuck =
	{
		"newunstuck"
	};
	
	@Override
	public String[] getByPassCommands()
	{
		return _newunstuck;
	}
}