package l2jorion.game.util;

import l2jorion.Config;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2World;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.network.PacketServer;
import l2jorion.game.network.clientpackets.Say2;
import l2jorion.game.network.serverpackets.CharInfo;
import l2jorion.game.network.serverpackets.CreatureSay;
import l2jorion.game.network.serverpackets.RelationChanged;

public final class Broadcast
{
	public static void toKnownPlayers(L2Character character, PacketServer packet)
	{
		for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
		{
			if (player == null)
			{
				continue;
			}
			
			if (player.isBot())
			{
				continue;
			}
			
			if (character instanceof L2PcInstance && !player.isGM() && (((L2PcInstance) character).getAppearance().getInvisible() || ((L2PcInstance) character).inObserverMode()))
			{
				continue;
			}
			
			try
			{
				player.sendPacket(packet);
				
				if (packet instanceof CharInfo && character instanceof L2PcInstance)
				{
					int relation = ((L2PcInstance) character).getRelation(player);
					if (character.getKnownList().getKnownRelations().get(player.getObjectId()) != null && character.getKnownList().getKnownRelations().get(player.getObjectId()) != relation)
					{
						player.sendPacket(new RelationChanged((L2PcInstance) character, relation, player.isAutoAttackable(character)));
					}
				}
			}
			catch (Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void toKnownPlayersInRadius(L2Character character, PacketServer packet, int radius)
	{
		toKnownPlayersInRadius(character, packet, radius, false);
	}
	
	public static void toKnownPlayersInRadius(L2Character character, PacketServer packet, int radius, boolean hideEffect)
	{
		if (radius < 0)
		{
			radius = 1500;
		}
		
		for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
		{
			if (player == null)
			{
				continue;
			}
			
			if (player.isBot())
			{
				continue;
			}
			
			if (hideEffect && player.isHidingEffects())
			{
				continue;
			}
			
			if (character.isInsideRadius(player, radius, false, false))
			{
				player.sendPacket(packet);
			}
		}
	}
	
	public static void toAllOnlinePlayers(String text)
	{
		toAllOnlinePlayers(text, false);
	}
	
	public static void toAllOnlinePlayers(String text, boolean isCritical)
	{
		toAllOnlinePlayers(new CreatureSay(0, isCritical ? Say2.CRITICAL_ANNOUNCE : Say2.ANNOUNCEMENT, "", text));
	}
	
	public static void toAllOnlinePlayers(PacketServer mov)
	{
		for (final L2PcInstance onlinePlayer : L2World.getInstance().getAllPlayers().values())
		{
			if (onlinePlayer != null)
			{
				if (onlinePlayer.isOnline() != 0)
				{
					onlinePlayer.sendPacket(mov);
				}
			}
		}
	}
}
