package l2jorion.game.model.templates;

import java.util.ArrayList;
import java.util.List;

/**
 * Template para configuración de auto farm adaptado para L2JOrion changeset 33 rev 1161
 */
public class AutoFarmTemplate
{
	private final int _templateId;
	private final String _name;
	private final String _description;
	
	private final List<Integer> _targetMobs;
	private final List<Integer> _pickupItems;
	private final List<Integer> _useSkills;
	
	private final int _minLevel;
	private final int _maxLevel;
	private final int _searchRange;
	private final int _pickupRange;
	
	private final boolean _autoHeal;
	private final boolean _autoPickup;
	private final boolean _autoAttack;
	private final boolean _useSoulshots;
	private final boolean _useSpiritshots;
	
	private final int _healHpPercent;
	private final int _healMpPercent;
	private final int _returnTownHpPercent;
	
	public AutoFarmTemplate(Builder builder)
	{
		_templateId = builder.templateId;
		_name = builder.name;
		_description = builder.description;
		_targetMobs = new ArrayList<>(builder.targetMobs);
		_pickupItems = new ArrayList<>(builder.pickupItems);
		_useSkills = new ArrayList<>(builder.useSkills);
		_minLevel = builder.minLevel;
		_maxLevel = builder.maxLevel;
		_searchRange = builder.searchRange;
		_pickupRange = builder.pickupRange;
		_autoHeal = builder.autoHeal;
		_autoPickup = builder.autoPickup;
		_autoAttack = builder.autoAttack;
		_useSoulshots = builder.useSoulshots;
		_useSpiritshots = builder.useSpiritshots;
		_healHpPercent = builder.healHpPercent;
		_healMpPercent = builder.healMpPercent;
		_returnTownHpPercent = builder.returnTownHpPercent;
	}
	
	// Getters
	public int getTemplateId()
	{
		return _templateId;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getDescription()
	{
		return _description;
	}
	
	public List<Integer> getTargetMobs()
	{
		return new ArrayList<>(_targetMobs);
	}
	
	public List<Integer> getPickupItems()
	{
		return new ArrayList<>(_pickupItems);
	}
	
	public List<Integer> getUseSkills()
	{
		return new ArrayList<>(_useSkills);
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public int getSearchRange()
	{
		return _searchRange;
	}
	
	public int getPickupRange()
	{
		return _pickupRange;
	}
	
	public boolean isAutoHeal()
	{
		return _autoHeal;
	}
	
	public boolean isAutoPickup()
	{
		return _autoPickup;
	}
	
	public boolean isAutoAttack()
	{
		return _autoAttack;
	}
	
	public boolean isUseSoulshots()
	{
		return _useSoulshots;
	}
	
	public boolean isUseSpiritshots()
	{
		return _useSpiritshots;
	}
	
	public int getHealHpPercent()
	{
		return _healHpPercent;
	}
	
	public int getHealMpPercent()
	{
		return _healMpPercent;
	}
	
	public int getReturnTownHpPercent()
	{
		return _returnTownHpPercent;
	}
	
	/**
	 * Verifica si el template es apropiado para el nivel del jugador
	 */
	public boolean isAppropriateLevel(int playerLevel)
	{
		return playerLevel >= _minLevel && playerLevel <= _maxLevel;
	}
	
	/**
	 * Verifica si un mob está en la lista de objetivos
	 */
	public boolean isTargetMob(int npcId)
	{
		return _targetMobs.contains(npcId);
	}
	
	/**
	 * Verifica si un item debe ser recogido
	 */
	public boolean shouldPickupItem(int itemId)
	{
		return _pickupItems.contains(itemId) || itemId == 57; // Siempre adena
	}
	
	/**
	 * Verifica si una skill debe ser usada
	 */
	public boolean shouldUseSkill(int skillId)
	{
		return _useSkills.contains(skillId);
	}
	
	/**
	 * Builder pattern para crear templates
	 */
	public static class Builder
	{
		public int templateId;
		private String name = "Default";
		private String description = "";
		private List<Integer> targetMobs = new ArrayList<>();
		private List<Integer> pickupItems = new ArrayList<>();
		private List<Integer> useSkills = new ArrayList<>();
		private int minLevel = 1;
		private int maxLevel = 85;
		private int searchRange = 600;
		private int pickupRange = 100;
		private boolean autoHeal = true;
		private boolean autoPickup = true;
		private boolean autoAttack = true;
		private boolean useSoulshots = false;
		private boolean useSpiritshots = false;
		private int healHpPercent = 70;
		private int healMpPercent = 50;
		private int returnTownHpPercent = 20;
		
		public Builder(int templateId)
		{
			this.templateId = templateId;
		}
		
		public Builder name(String name)
		{
			this.name = name;
			return this;
		}
		
		public Builder description(String description)
		{
			this.description = description;
			return this;
		}
		
		public Builder addTargetMob(int npcId)
		{
			this.targetMobs.add(npcId);
			return this;
		}
		
		public Builder addPickupItem(int itemId)
		{
			this.pickupItems.add(itemId);
			return this;
		}
		
		public Builder addUseSkill(int skillId)
		{
			this.useSkills.add(skillId);
			return this;
		}
		
		public Builder levelRange(int min, int max)
		{
			this.minLevel = min;
			this.maxLevel = max;
			return this;
		}
		
		public Builder searchRange(int range)
		{
			this.searchRange = range;
			return this;
		}
		
		public Builder pickupRange(int range)
		{
			this.pickupRange = range;
			return this;
		}
		
		public Builder autoHeal(boolean enable)
		{
			this.autoHeal = enable;
			return this;
		}
		
		public Builder autoPickup(boolean enable)
		{
			this.autoPickup = enable;
			return this;
		}
		
		public Builder autoAttack(boolean enable)
		{
			this.autoAttack = enable;
			return this;
		}
		
		public Builder useSoulshots(boolean enable)
		{
			this.useSoulshots = enable;
			return this;
		}
		
		public Builder useSpiritshots(boolean enable)
		{
			this.useSpiritshots = enable;
			return this;
		}
		
		public Builder healHpPercent(int percent)
		{
			this.healHpPercent = percent;
			return this;
		}
		
		public Builder healMpPercent(int percent)
		{
			this.healMpPercent = percent;
			return this;
		}
		
		public Builder returnTownHpPercent(int percent)
		{
			this.returnTownHpPercent = percent;
			return this;
		}
		
		public AutoFarmTemplate build()
		{
			return new AutoFarmTemplate(this);
		}
	}
}