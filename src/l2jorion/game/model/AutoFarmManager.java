package l2jorion.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import l2jorion.game.ai.CtrlIntention;
import l2jorion.game.model.actor.instance.L2ItemInstance;
import l2jorion.game.model.actor.instance.L2MonsterInstance;
import l2jorion.game.model.actor.instance.L2PcInstance;
import l2jorion.game.model.tables.ItemDropTable;
import l2jorion.game.model.tables.MobTargetTable;
import l2jorion.game.model.templates.AutoFarmTemplate;
import l2jorion.game.model.templates.AutoFarmTemplateTable;

/**
 * Manager avanzado para auto farm adaptado para L2JOrion changeset 33 rev 1161
 */
public class AutoFarmManager
{
	private static final Logger _log = Logger.getLogger(AutoFarmManager.class.getName());
	
	private final L2PcInstance _player;
	private final Inertia _inertia;
	
	private AutoFarmTemplate _currentTemplate;
	private L2Object _currentTarget;
	
	private long _lastTargetSearch = 0;
	private long _lastPickupAttempt = 0;
	private long _lastHealAttempt = 0;
	private long _lastShotsCheck = 0;
	
	private int _mobsKilled = 0;
	private int _itemsPickedUp = 0;
	private long _adenaGained = 0;
	
	public AutoFarmManager(L2PcInstance player, Inertia inertia)
	{
		_player = player;
		_inertia = inertia;
		loadBestTemplate();
	}
	
	/**
	 * Carga el mejor template para el nivel del jugador
	 */
	private void loadBestTemplate()
	{
		_currentTemplate = AutoFarmTemplateTable.getInstance().getBestTemplateForLevel(_player.getLevel());
		_log.info("Auto Farm: Cargado template '" + _currentTemplate.getName() + "' para " + _player.getName());
	}
	
	/**
	 * Procesa el ciclo principal de auto farm
	 */
	public void process()
	{
		if (!_inertia.isActive() || _player.isDead())
		{
			return;
		}
		
		try
		{
			// Verificar shots
			if (System.currentTimeMillis() - _lastShotsCheck > 5000)
			{
				checkAndUseShots();
				_lastShotsCheck = System.currentTimeMillis();
			}
			
			// Auto heal
			if (_currentTemplate.isAutoHeal() && System.currentTimeMillis() - _lastHealAttempt > 2000)
			{
				processAutoHeal();
				_lastHealAttempt = System.currentTimeMillis();
			}
			
			// Auto pickup
			if (_currentTemplate.isAutoPickup() && System.currentTimeMillis() - _lastPickupAttempt > 1000)
			{
				processAutoPickup();
				_lastPickupAttempt = System.currentTimeMillis();
			}
			
			// Auto attack
			if (_currentTemplate.isAutoAttack() && System.currentTimeMillis() - _lastTargetSearch > 1500)
			{
				processAutoAttack();
				_lastTargetSearch = System.currentTimeMillis();
			}
		}
		catch (Exception e)
		{
			_log.warning("Error en AutoFarmManager para " + _player.getName() + ": " + e.getMessage());
		}
	}
	
	/**
	 * Procesa auto heal avanzado
	 */
	private void processAutoHeal()
	{
		double hpPercent = (_player.getCurrentHp() / _player.getMaxHp()) * 100;
		double mpPercent = (_player.getCurrentMp() / _player.getMaxMp()) * 100;
		
		// Verificar si necesita volver a town
		if (hpPercent < _currentTemplate.getReturnTownHpPercent())
		{
			_log.info("Auto Farm: " + _player.getName() + " necesita volver a town (HP muy bajo)");
			// Aquí podrías implementar auto return to town
			return;
		}
		
		// Heal HP
		if (hpPercent < _currentTemplate.getHealHpPercent())
		{
			L2Skill healSkill = getBestHealSkill();
			if (healSkill != null && _player.getCurrentMp() >= healSkill.getMpConsume())
			{
				_player.useMagic(healSkill, false, false);
				return;
			}
			
			// Si no tiene skill, usar poción
			useHealingPotion();
		}
		
		// Heal MP
		if (mpPercent < _currentTemplate.getHealMpPercent())
		{
			useManaPotion();
		}
	}
	
	/**
	 * Obtiene la mejor skill de curación
	 */
	private L2Skill getBestHealSkill()
	{
		L2Skill bestSkill = null;
		int bestHeal = 0;
		
		L2Skill[] skills = _player.getAllSkills();
		for (L2Skill skill : skills)
		{
			if (isHealSkill(skill.getId()) && _player.getCurrentMp() >= skill.getMpConsume())
			{
				int healPower = skill.getPower();
				if (healPower > bestHeal)
				{
					bestHeal = healPower;
					bestSkill = skill;
				}
			}
		}
		
		return bestSkill;
	}
	
	/**
	 * Verifica si una skill es de curación
	 */
	private boolean isHealSkill(int skillId)
	{
		// IDs comunes de skills de heal en L2
		return skillId == 1002 || skillId == 1003 || skillId == 1218 || skillId == 1217 || skillId == 1015 || skillId == 1016 || skillId == 1032 || skillId == 1033;
	}
	
	/**
	 * Usa poción de curación
	 */
	private void useHealingPotion()
	{
		// IDs de pociones de HP comunes
		int[] healPotions =
		{
			1060,
			1061,
			1073,
			1539,
			1540
		}; // Greater Healing Potion, etc.
		
		for (int potionId : healPotions)
		{
			L2ItemInstance potion = _player.getInventory().getItemByItemId(potionId);
			if (potion != null)
			{
				_player.useEquippableItem(potion, false);
				break;
			}
		}
	}
	
	/**
	 * Usa poción de mana
	 */
	private void useManaPotion()
	{
		// IDs de pociones de MP comunes
		int[] manaPotions =
		{
			726,
			728,
			1374,
			1375
		}; // Mana Potion, Greater Mana Potion, etc.
		
		for (int potionId : manaPotions)
		{
			L2ItemInstance potion = _player.getInventory().getItemByItemId(potionId);
			if (potion != null)
			{
				_player.useEquippableItem(potion, false);
				break;
			}
		}
	}
	
	/**
	 * Procesa auto pickup avanzado
	 */
	private void processAutoPickup()
	{
		List<L2ItemInstance> nearbyItems = getNearbyItems();
		
		for (L2ItemInstance item : nearbyItems)
		{
			if (shouldPickupItem(item))
			{
				_player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
				_itemsPickedUp++;
				
				if (item.getItemId() == 57) // Adena
				{
					_adenaGained += item.getCount();
				}
				
				break; // Solo un item por ciclo
			}
		}
	}
	
	/**
	 * Obtiene items cercanos
	 */
	private List<L2ItemInstance> getNearbyItems()
	{
		List<L2ItemInstance> items = new ArrayList<>();
		
		for (L2Object obj : _player.getKnownList().getKnownObjects().values())
		{
			if (obj instanceof L2ItemInstance)
			{
				L2ItemInstance item = (L2ItemInstance) obj;
				double distance = calculateDistance(_player, item);
				
				if (distance <= _currentTemplate.getPickupRange())
				{
					items.add(item);
				}
			}
		}
		
		return items;
	}
	
	/**
	 * Verifica si debe recoger un item
	 */
	private boolean shouldPickupItem(L2ItemInstance item)
	{
		int itemId = item.getItemId();
		
		// Siempre recoger adena
		if (itemId == 57)
		{
			return true;
		}
		
		// Verificar template
		if (_currentTemplate.shouldPickupItem(itemId))
		{
			return true;
		}
		
		// Verificar tabla de items valiosos
		return ItemDropTable.getInstance().isValuableItem(itemId);
	}
	
	/**
	 * Procesa auto attack avanzado
	 */
	private void processAutoAttack()
	{
		// Si ya está atacando, continuar
		if (_player.isAttackingNow() && _currentTarget != null && !((L2MonsterInstance) _currentTarget).isDead())
		{
			return;
		}
		
		// Buscar nuevo objetivo
		L2MonsterInstance target = findBestTarget();
		if (target != null)
		{
			_currentTarget = target;
			_player.setTarget(target);
			_player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
	}
	
	/**
	 * Encuentra el mejor objetivo para atacar
	 */
	private L2MonsterInstance findBestTarget()
	{
		L2MonsterInstance bestTarget = null;
		double nearestDistance = Double.MAX_VALUE;
		int bestPriority = -1;
		
		for (L2Object obj : _player.getKnownList().getKnownObjects().values())
		{
			if (obj instanceof L2MonsterInstance)
			{
				L2MonsterInstance monster = (L2MonsterInstance) obj;
				
				if (isValidTarget(monster))
				{
					double distance = calculateDistance(_player, monster);
					int priority = getTargetPriority(monster);
					
					// Priorizar por: 1) Prioridad, 2) Distancia
					if (priority > bestPriority || (priority == bestPriority && distance < nearestDistance))
					{
						bestTarget = monster;
						nearestDistance = distance;
						bestPriority = priority;
					}
				}
			}
		}
		
		return bestTarget;
	}
	
	/**
	 * Verifica si un objetivo es válido
	 */
	private boolean isValidTarget(L2MonsterInstance monster)
	{
		if (monster.isDead())
		{
			return false;
		}
		
		// Verificar distancia
		double distance = calculateDistance(_player, monster);
		if (distance > _currentTemplate.getSearchRange())
		{
			return false;
		}
		
		// Verificar si es farmable
		int npcId = monster.getNpcId();
		if (!MobTargetTable.getInstance().isFarmableMob(npcId))
		{
			return false;
		}
		
		// Verificar nivel apropiado
		if (!MobTargetTable.getInstance().isAppropriateLevel(npcId, _player.getLevel()))
		{
			return false;
		}
		
		// Verificar template específico
		if (!_currentTemplate.getTargetMobs().isEmpty() && !_currentTemplate.isTargetMob(npcId))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Obtiene la prioridad de un objetivo
	 */
	private int getTargetPriority(L2MonsterInstance monster)
	{
		int priority = 0;
		
		// Prioridad por HP (menos HP = más prioridad)
		double hpPercent = (monster.getCurrentHp() / monster.getMaxHp()) * 100;
		if (hpPercent < 50)
		{
			priority += 2;
		}
		else if (hpPercent < 75)
		{
			priority += 1;
		}
		
		// Prioridad si ya está atacando al jugador
		if (monster.getTarget() == _player)
		{
			priority += 3;
		}
		
		return priority;
	}
	
	/**
	 * Verifica y usa shots automáticamente
	 */
	private void checkAndUseShots()
	{
		if (_currentTemplate.isUseSoulshots())
		{
			// Verificar soulshots
			// Implementar lógica de soulshots aquí
		}
		
		if (_currentTemplate.isUseSpiritshots())
		{
			// Verificar spiritshots
			// Implementar lógica de spiritshots aquí
		}
	}
	
	/**
	 * Calcula distancia entre dos objetos
	 */
	private double calculateDistance(L2Object obj1, L2Object obj2)
	{
		return Math.sqrt(Math.pow(obj1.getX() - obj2.getX(), 2) + Math.pow(obj1.getY() - obj2.getY(), 2));
	}
	
	/**
	 * Callback cuando mata un mob
	 */
	public void onKill(L2MonsterInstance victim)
	{
		_mobsKilled++;
		_log.fine("Auto Farm: " + _player.getName() + " mató " + victim.getName() + " (Total: " + _mobsKilled + ")");
	}
	
	/**
	 * Obtiene estadísticas de farm
	 */
	public String getStats()
	{
		return String.format("Mobs: %d | Items: %d | Adena: %d", _mobsKilled, _itemsPickedUp, _adenaGained);
	}
	
	/**
	 * Resetea estadísticas
	 */
	public void resetStats()
	{
		_mobsKilled = 0;
		_itemsPickedUp = 0;
		_adenaGained = 0;
	}
	
	// Getters
	public AutoFarmTemplate getCurrentTemplate()
	{
		return _currentTemplate;
	}
	
	public int getMobsKilled()
	{
		return _mobsKilled;
	}
	
	public int getItemsPickedUp()
	{
		return _itemsPickedUp;
	}
	
	public long getAdenaGained()
	{
		return _adenaGained;
	}
}