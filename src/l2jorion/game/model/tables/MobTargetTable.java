package l2jorion.game.model.tables;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tabla de objetivos de mobs para auto farm adaptada para L2JOrion changeset 33 rev 1161
 */
public class MobTargetTable
{
	private static MobTargetTable _instance;
	
	private final Set<Integer> _farmableMobs = ConcurrentHashMap.newKeySet();
	private final Set<Integer> _avoidMobs = ConcurrentHashMap.newKeySet();
	private final Map<Integer, Integer> _mobLevels = new ConcurrentHashMap<>();
	
	public static MobTargetTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new MobTargetTable();
		}
		return _instance;
	}
	
	private MobTargetTable()
	{
		loadDefaultMobs();
	}
	
	/**
	 * Carga mobs por defecto para farm
	 */
	private void loadDefaultMobs()
	{
		// Mobs farmables comunes (IDs aproximados)
		addFarmableMob(20001, 1); // Orc
		addFarmableMob(20002, 2); // Orc Archer
		addFarmableMob(20003, 3); // Orc Fighter
		addFarmableMob(20004, 4); // Orc Raider
		addFarmableMob(20005, 5); // Orc Scout
		
		// Undead
		addFarmableMob(20026, 8); // Skeleton Warrior
		addFarmableMob(20027, 10); // Skeleton Archer
		addFarmableMob(20028, 12); // Skeleton
		
		// Dion area
		addFarmableMob(20103, 15); // Giant Spider
		addFarmableMob(20104, 16); // Talon Spider
		addFarmableMob(20105, 17); // Blade Spider
		
		// Gludin area
		addFarmableMob(20006, 6); // Goblin
		addFarmableMob(20007, 7); // Goblin Warrior
		addFarmableMob(20008, 8); // Goblin Brigand
		
		// Mobs a evitar (muy peligrosos)
		addAvoidMob(25001); // Queen Ant
		addAvoidMob(25002); // Core
		addAvoidMob(25003); // Orfen
		addAvoidMob(25004); // Zaken
		addAvoidMob(25005); // Baium
		addAvoidMob(25006); // Antharas
		addAvoidMob(25007); // Valakas
	}
	
	/**
	 * Añade un mob farmable
	 * @param npcId El ID del NPC a añadir
	 * @param level El nivel del mob
	 */
	public void addFarmableMob(int npcId, int level)
	{
		_farmableMobs.add(npcId);
		_mobLevels.put(npcId, level);
	}
	
	/**
	 * Añade un mob a evitar
	 * @param npcId El ID del NPC a evitar
	 */
	public void addAvoidMob(int npcId)
	{
		_avoidMobs.add(npcId);
		_farmableMobs.remove(npcId); // Asegurar que no esté en farmables
	}
	
	/**
	 * Verifica si un mob es farmable
	 * @param npcId El ID del NPC a verificar
	 * @return true si el mob es farmable y no debe evitarse
	 */
	public boolean isFarmableMob(int npcId)
	{
		return _farmableMobs.contains(npcId) && !_avoidMobs.contains(npcId);
	}
	
	/**
	 * Verifica si un mob debe evitarse
	 * @param npcId El ID del NPC a verificar
	 * @return true si el mob debe evitarse
	 */
	public boolean shouldAvoidMob(int npcId)
	{
		return _avoidMobs.contains(npcId);
	}
	
	/**
	 * Obtiene el nivel de un mob
	 * @param npcId El ID del NPC
	 * @return El nivel del mob o 1 si no se encuentra
	 */
	public int getMobLevel(int npcId)
	{
		return _mobLevels.getOrDefault(npcId, 1);
	}
	
	/**
	 * Verifica si el mob es apropiado para el nivel del jugador
	 * @param npcId El ID del NPC a verificar
	 * @param playerLevel El nivel del jugador
	 * @return true si la diferencia de nivel es <= 10
	 */
	public boolean isAppropriateLevel(int npcId, int playerLevel)
	{
		int mobLevel = getMobLevel(npcId);
		int levelDiff = Math.abs(playerLevel - mobLevel);
		return levelDiff <= 10; // Máximo 10 niveles de diferencia
	}
	
	/**
	 * Remueve un mob farmable
	 * @param npcId El ID del NPC a remover
	 */
	public void removeFarmableMob(int npcId)
	{
		_farmableMobs.remove(npcId);
		_mobLevels.remove(npcId);
	}
	
	/**
	 * Obtiene todos los mobs farmables
	 * @return Set inmutable con todos los IDs de mobs farmables
	 */
	public Set<Integer> getFarmableMobs()
	{
		Set<Integer> result = new java.util.HashSet<>();
		result.addAll(_farmableMobs);
		return java.util.Collections.unmodifiableSet(result);
	}
	
	/**
	 * Obtiene todos los mobs a evitar
	 * @return Set inmutable con todos los IDs de mobs a evitar
	 */
	public Set<Integer> getAvoidMobs()
	{
		Set<Integer> result = new java.util.HashSet<>();
		result.addAll(_avoidMobs);
		return java.util.Collections.unmodifiableSet(result);
	}
}