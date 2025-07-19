package l2jorion.game.ai.additional.invidual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l2jorion.Config;
import l2jorion.game.model.L2Party;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2NpcInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.quest.Quest;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.ExShowScreenMessage;
import l2jorion.game.network.serverpackets.InventoryUpdate;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.util.Util;

public class CustomBossForNoble extends Quest implements Runnable
{
	private static final int BOSS = 25325;
	// Noblesse Tiara ID
	private static final int NOBLESSE_TIARA = 7694;
	// Máximo de personajes que pueden recibir el beneficio
	private static final int MAX_BENEFICIARIES = 3;
	
	public CustomBossForNoble(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		addEventId(BOSS, Quest.QuestEventType.ON_KILL);
	}
	
	/**
	 * Verifica si el jugador ya tiene la tiara noblesse
	 * @param player El jugador a verificar
	 * @return true si el jugador tiene la tiara noblesse, false en caso contrario
	 */
	private boolean hasNoblesseTiara(L2PcInstance player)
	{
		return player.getInventory().getItemByItemId(NOBLESSE_TIARA) != null;
	}
	
	/**
	 * Verifica si el jugador ya es elegible para recibir el beneficio
	 * @param player El jugador a verificar
	 * @return true si el jugador es elegible (no es noble y no tiene tiara), false en caso contrario
	 */
	private boolean isEligibleForBenefit(L2PcInstance player)
	{
		return !player.isNoble() && !hasNoblesseTiara(player);
	}
	
	/**
	 * Obtiene la IP del jugador
	 * @param player El jugador del cual obtener la IP
	 * @return La dirección IP del jugador como String, o null si no se puede obtener
	 */
	private String getPlayerIP(L2PcInstance player)
	{
		try
		{
			if (player.getClient() != null && player.getClient().getConnection() != null)
			{
				return player.getClient().getConnection().getInetAddress().getHostAddress();
			}
		}
		catch (Exception e)
		{
			// Log error if needed
		}
		return null;
	}
	
	/**
	 * Filtra los jugadores elegibles evitando dual IP
	 * @param players Lista de jugadores a filtrar
	 * @return Lista de jugadores elegibles sin duplicados de IP
	 */
	private List<L2PcInstance> filterEligiblePlayers(List<L2PcInstance> players)
	{
		List<L2PcInstance> eligible = new ArrayList<>();
		Set<String> usedIPs = new HashSet<>();
		
		for (L2PcInstance player : players)
		{
			if (!isEligibleForBenefit(player))
			{
				continue;
			}
			
			String playerIP = getPlayerIP(player);
			if (playerIP == null)
			{
				continue;
			}
			
			// Si ya hay un jugador con esta IP, rechazamos este personaje
			if (usedIPs.contains(playerIP))
			{
				// Mensaje en rojo para dual IP
				player.sendPacket(new ExShowScreenMessage("Cannot receive Noblesse benefit: Dual IP detected!", 4000, 0x02, false));
				player.sendMessage("\\c4Cannot receive Noblesse benefit: Another character from your IP already received it.");
				continue;
			}
			
			// Agregamos la IP a la lista de usadas SOLO cuando agregamos al jugador
			usedIPs.add(playerIP);
			eligible.add(player);
			
			// Límite de 3 jugadores
			if (eligible.size() >= MAX_BENEFICIARIES)
			{
				break;
			}
		}
		
		return eligible;
	}
	
	/**
	 * Otorga el beneficio de noblesse a un jugador
	 * @param player El jugador que recibirá el beneficio de noblesse
	 */
	private void grantNoblesseBenefit(L2PcInstance player)
	{
		player.setNoble(true);
		player.sendMessage("Congratulations! You've got The Nobless status");
		player.sendPacket(new ExShowScreenMessage("Congratulations! You've got The Nobless status", 3000, 0x02, false));
		
		PlaySound playSound = new PlaySound("ItemSound.quest_fanfare_1");
		player.sendPacket(playSound);
		player.broadcastUserInfo();
		
		// Agregar la tiara noblesse
		L2ItemInstance newitem = player.getInventory().addItem("Tiara", NOBLESSE_TIARA, 1, player, null);
		InventoryUpdate playerIU = new InventoryUpdate();
		playerIU.addItem(newitem);
		player.sendPacket(playerIU);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
		sm.addItemName(NOBLESSE_TIARA);
		player.sendPacket(sm);
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		L2Party party = killer.getParty();
		
		if (party != null)
		{
			List<L2PcInstance> partyMembers = new ArrayList<>();
			
			// Recopilar miembros de la party válidos
			for (L2PcInstance member : party.getPartyMembers())
			{
				if (member == null)
				{
					continue;
				}
				
				if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, member, true))
				{
					continue;
				}
				
				partyMembers.add(member);
			}
			
			// Filtrar jugadores elegibles (sin dual IP, sin noblesse)
			List<L2PcInstance> eligiblePlayers = filterEligiblePlayers(partyMembers);
			
			// Otorgar beneficio a los jugadores elegibles
			int beneficiariesCount = 0;
			for (L2PcInstance eligible : eligiblePlayers)
			{
				grantNoblesseBenefit(eligible);
				beneficiariesCount++;
			}
			
			// Notificar a los jugadores que no eran elegibles
			for (L2PcInstance member : partyMembers)
			{
				if (!eligiblePlayers.contains(member))
				{
					if (member.isNoble() || hasNoblesseTiara(member))
					{
						member.sendMessage("You already have Noblesse status or Noblesse Tiara.");
					}
				}
			}
			
			// Notificar si se alcanzó el límite
			if (beneficiariesCount >= MAX_BENEFICIARIES)
			{
				for (L2PcInstance member : partyMembers)
				{
					member.sendMessage("Noblesse benefit limit reached. Only " + MAX_BENEFICIARIES + " characters can receive the benefit.");
				}
			}
		}
		else
		{
			// Jugador solo
			if (isEligibleForBenefit(killer))
			{
				grantNoblesseBenefit(killer);
			}
			else
			{
				killer.sendMessage("You already have Noblesse status or Noblesse Tiara.");
			}
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public void run()
	{
	}
}