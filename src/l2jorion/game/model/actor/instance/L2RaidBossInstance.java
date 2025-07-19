/*
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
package l2jorion.game.model.actor.instance;

import java.util.HashSet;
import java.util.Set;

import l2jorion.Config;
import l2jorion.game.enums.AchType;
import l2jorion.game.managers.GrandBossManager;
import l2jorion.game.managers.RaidBossPointsManager;
import l2jorion.game.managers.RaidBossSpawnManager;
import l2jorion.game.model.L2Character;
import l2jorion.game.model.L2Summon;
import l2jorion.game.model.entity.Announcements;
import l2jorion.game.model.spawn.AutoSpawn;
import l2jorion.game.model.spawn.L2Spawn;
import l2jorion.game.model.zone.type.L2BossZone;
import l2jorion.game.network.SystemMessageId;
import l2jorion.game.network.serverpackets.PlaySound;
import l2jorion.game.network.serverpackets.SystemMessage;
import l2jorion.game.templates.L2NpcTemplate;
import l2jorion.game.thread.ThreadPoolManager;
import l2jorion.log.Log;
import l2jorion.util.random.Rnd;

public final class L2RaidBossInstance extends L2MonsterInstance
{
	private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 25000;
	
	// Porcentajes de HP para anuncios
	private static final int[] HP_ANNOUNCE_THRESHOLDS =
	{
		80,
		60,
		40,
		20,
		10,
		5
	};
	
	private RaidBossSpawnManager.StatusEnum _raidStatus;
	protected static L2BossZone _zone;
	
	// Trackear qué porcentajes de HP ya fueron anunciados
	private Set<Integer> _announcedHpPercentages = new HashSet<>();
	
	public L2RaidBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsRaid(true);
	}
	
	@Override
	protected int getMaintenanceInterval()
	{
		return RAIDBOSS_MAINTENANCE_INTERVAL;
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		super.reduceCurrentHp(damage, attacker, awake);
		
		// Verificar porcentaje de HP para anuncios
		checkHpPercentageAnnouncement();
	}
	
	/**
	 * Verificar el porcentaje actual de HP y anunciar si alcanza un threshold
	 */
	private void checkHpPercentageAnnouncement()
	{
		if (isDead())
		{
			return;
		}
		
		double currentHpPercent = (getCurrentHp() / getMaxHp()) * 100;
		
		for (int threshold : HP_ANNOUNCE_THRESHOLDS)
		{
			if (currentHpPercent <= threshold && !_announcedHpPercentages.contains(threshold))
			{
				_announcedHpPercentages.add(threshold);
				announceHpPercentage(threshold);
				break; // Solo anunciar un threshold por verificación
			}
		}
	}
	
	/**
	 * Anunciar el porcentaje de HP del raid boss
	 * @param percentage el porcentaje de HP a anunciar
	 */
	private void announceHpPercentage(int percentage)
	{
		String message = "Raid Boss " + getName() + " tiene " + percentage + "% de vida restante!";
		
		if (Config.ANNOUNCE_RB_KILLER_INFO)
		{
			Announcements.getInstance().announceWithServerName(message);
		}
		
		// Opcional: Log del anuncio de HP
		if (Config.L2EMI_CUSTOM)
		{
			Log.add("Anuncio Vida: " + getName() + " - " + percentage + "%", "RaidBosses");
		}
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		L2PcInstance player = null;
		
		if (killer instanceof L2PcInstance)
		{
			player = (L2PcInstance) killer;
		}
		else if (killer instanceof L2Summon)
		{
			player = ((L2Summon) killer).getOwner();
		}
		
		broadcastPacket(new PlaySound("systemmsg_e.1209"));
		broadcastPacket(new SystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
		
		if (player != null && (getNpcId() != 22215 && getNpcId() != 22216 && getNpcId() != 22217 && getNpcId() != 22318 && getNpcId() != 22319))
		{
			if (Config.L2EMI_CUSTOM)
			{
				final String text = "Last hit: " + player.getName() + " (lv: " + player.getLevel() + ")";
				Log.add(text, "RaidBosses");
			}
			
			if (Config.ANNOUNCE_RB_KILLER_INFO)
			{
				String deathMessage;
				if (player.getClan() != null)
				{
					deathMessage = "Raid Boss " + getName() + " has been defeated! " + "Killed by: " + player.getName() + " (Level " + player.getLevel() + ") " + "del clan " + player.getClan().getName();
				}
				else
				{
					deathMessage = "Raid Boss " + getName() + " has been defeated! " + "Killed by: " + player.getName() + " (Level " + player.getLevel() + ") " + "[Sin Clan]";
				}
				
				Announcements.getInstance().announceWithServerName(deathMessage);
			}
			
			if (Config.L2LIMIT_CUSTOM)
			{
				if (getLevel() >= 40)
				{
					if (player.getClan() != null)
					{
						player.getClan().setReputationScore(player.getClan().getReputationScore() + Rnd.get(50, 100), true);
					}
					player.addItem("AutoLoot", 6392, 1, this, true);
				}
			}
			
			if (player.getParty() != null)
			{
				int points = (getLevel() / 2) + Rnd.get(-5, 5);
				if (Config.RON_CUSTOM)
				{
					points = getLevel();
				}
				
				for (L2PcInstance member : player.getParty().getPartyMembers())
				{
					if (Config.RON_CUSTOM)
					{
						member.setClanRaidPoints(member.getClanRaidPoints() + points);
						member.sendMessage("Has obtenido " + points + " raid points.");
					}
					
					RaidBossPointsManager.addPoints(member, getNpcId(), points);
					member.getAchievement().increase(AchType.RAIDBOSS);
					
					// Daily
					member.getAchievement().increase(AchType.DAILY_BOSS, 1, true, true, true, getNpcId());
					
					if (Config.L2LIMIT_CUSTOM)
					{
						member.setWeeklyBoardRaidPoints(member.getWeeklyBoardRaidPoints() + ((getLevel() / 2) + Rnd.get(-5, 5)));
					}
				}
			}
			else
			{
				int points = (getLevel() / 2) + Rnd.get(-5, 5);
				if (Config.RON_CUSTOM)
				{
					points = getLevel();
					
					player.setClanRaidPoints(player.getClanRaidPoints() + points);
					player.sendMessage("Has obtenido " + points + " puntos de incursión.");
				}
				RaidBossPointsManager.addPoints(player, getNpcId(), points);
				player.getAchievement().increase(AchType.RAIDBOSS);
				
				// Daily
				player.getAchievement().increase(AchType.DAILY_BOSS, 1, true, true, true, getNpcId());
				
				if (Config.L2LIMIT_CUSTOM)
				{
					player.setWeeklyBoardRaidPoints(player.getWeeklyBoardRaidPoints() + ((getLevel() / 2) + Rnd.get(-5, 5)));
				}
			}
		}
		
		// Resetear anuncios de HP para el próximo spawn
		_announcedHpPercentages.clear();
		
		if (!getSpawn().is_customBossInstance())
		{
			RaidBossSpawnManager.getInstance().updateStatus(this, true);
			
			if (Config.RON_CUSTOM)
			{
				_zone = GrandBossManager.getInstance().getZone(getX(), getY(), getZ());
				if (_zone != null)
				{
					_zone.updateZoneStatusForCharactersInside();
					_zone.setZoneEnabled(false, false);
				}
			}
		}
		
		// Check auto spawn instance
		AutoSpawn.updateStatus(this, true);
		
		return true;
	}
	
	@Override
	protected void manageMaintenance()
	{
		_minionList.spawnMinions();
		
		if (_minionMaintainTask == null)
		{
			_minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
			{
				L2Spawn bossSpawn = getSpawn();
				
				int rb_lock_range = Config.RBLOCKRAGE;
				
				if (rb_lock_range >= 100 && !isInsideRadius(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), rb_lock_range, true, false))
				{
					teleToLocation(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), false);
					
					if (Config.HEAL_RAIDBOSS)
					{
						healFull();
						// Resetear anuncios cuando el boss es curado
						_announcedHpPercentages.clear();
					}
				}
				
				callMinionsBack();
				_minionList.maintainMinions();
				
			}, 1000, getMaintenanceInterval());
		}
	}
	
	public void setRaidStatus(RaidBossSpawnManager.StatusEnum status)
	{
		_raidStatus = status;
	}
	
	public RaidBossSpawnManager.StatusEnum getRaidStatus()
	{
		return _raidStatus;
	}
	
	public void healFull()
	{
		super.setCurrentHp(super.getMaxHp());
		super.setCurrentMp(super.getMaxMp());
		// Resetear anuncios de HP cuando es curado completamente
		_announcedHpPercentages.clear();
	}
	
	@Override
	public boolean isMonster()
	{
		return false;
	}
}