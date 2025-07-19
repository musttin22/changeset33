package l2jorion.game.model.templates;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tabla de templates de auto farm adaptada para L2JOrion changeset 33 rev 1161
 */
public class AutoFarmTemplateTable
{
	private static AutoFarmTemplateTable _instance;
	
	private final Map<Integer, AutoFarmTemplate> _templates = new ConcurrentHashMap<>();
	
	public static AutoFarmTemplateTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new AutoFarmTemplateTable();
		}
		return _instance;
	}
	
	private AutoFarmTemplateTable()
	{
		loadDefaultTemplates();
	}
	
	/**
	 * Carga templates por defecto
	 */
	private void loadDefaultTemplates()
	{
		// Template para newbies (1-20)
		AutoFarmTemplate newbie = new AutoFarmTemplate.Builder(1).name("Newbie Farm").description("Para jugadores nivel 1-20").levelRange(1, 20).addTargetMob(20001) // Orc
		.addTargetMob(20002) // Orc Archer
		.addTargetMob(20006) // Goblin
		.addPickupItem(57) // Adena
		.addPickupItem(1458) // Crystal D
		.searchRange(400).healHpPercent(80).build();
		_templates.put(1, newbie);
		
		// Template para low level (20-40)
		AutoFarmTemplate lowLevel = new AutoFarmTemplate.Builder(2).name("Low Level Farm").description("Para jugadores nivel 20-40").levelRange(20, 40).addTargetMob(20026) // Skeleton Warrior
		.addTargetMob(20027) // Skeleton Archer
		.addTargetMob(20103) // Giant Spider
		.addPickupItem(57) // Adena
		.addPickupItem(1458) // Crystal D
		.addPickupItem(1459) // Crystal C
		.searchRange(500).healHpPercent(75).build();
		_templates.put(2, lowLevel);
		
		// Template para mid level (40-60)
		AutoFarmTemplate midLevel = new AutoFarmTemplate.Builder(3).name("Mid Level Farm").description("Para jugadores nivel 40-60").levelRange(40, 60).addTargetMob(20104) // Talon Spider
		.addTargetMob(20105) // Blade Spider
		.addPickupItem(57) // Adena
		.addPickupItem(1459) // Crystal C
		.addPickupItem(1460) // Crystal B
		.addPickupItem(8600) // Herb of Life
		.searchRange(600).healHpPercent(70).useSoulshots(true).build();
		_templates.put(3, midLevel);
		
		// Template para high level (60-85)
		AutoFarmTemplate highLevel = new AutoFarmTemplate.Builder(4).name("High Level Farm").description("Para jugadores nivel 60-85").levelRange(60, 85).addPickupItem(57) // Adena
		.addPickupItem(1460) // Crystal B
		.addPickupItem(1461) // Crystal A
		.addPickupItem(1462) // Crystal S
		.addPickupItem(8600) // Herb of Life
		.addPickupItem(8601) // Herb of Mana
		.searchRange(700).healHpPercent(65).useSoulshots(true).useSpiritshots(true).build();
		_templates.put(4, highLevel);
	}
	
	/**
	 * Obtiene un template por ID
	 */
	public AutoFarmTemplate getTemplate(int templateId)
	{
		return _templates.get(templateId);
	}
	
	/**
	 * Obtiene el template más apropiado para un nivel
	 */
	public AutoFarmTemplate getBestTemplateForLevel(int playerLevel)
	{
		for (AutoFarmTemplate template : _templates.values())
		{
			if (template.isAppropriateLevel(playerLevel))
			{
				return template;
			}
		}
		return _templates.get(1); // Fallback al template newbie
	}
	
	/**
	 * Añade un template
	 */
	public void addTemplate(AutoFarmTemplate template)
	{
		_templates.put(template.getTemplateId(), template);
	}
	
	/**
	 * Remueve un template
	 */
	public void removeTemplate(int templateId)
	{
		_templates.remove(templateId);
	}
	
	/**
	 * Obtiene todos los templates
	 */
	public Collection<AutoFarmTemplate> getAllTemplates()
	{
		return _templates.values();
	}
	
	/**
	 * Verifica si existe un template
	 */
	public boolean hasTemplate(int templateId)
	{
		return _templates.containsKey(templateId);
	}
}