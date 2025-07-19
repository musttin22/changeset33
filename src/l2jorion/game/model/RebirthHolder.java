package l2jorion.game.model;

import l2jorion.game.templates.StatsSet;

public class RebirthHolder
{
	private final int _id;
	private final int _requiredItemId;
	private final int _requiredItemAmount;
	private final String _icon;
	private final String _desc;
	
	public RebirthHolder(StatsSet set)
	{
		_id = set.getInteger("id", 0);
		_requiredItemId = set.getInteger("requiredItemId");
		_requiredItemAmount = set.getInteger("requiredItemAmount");
		_icon = set.getString("icon", "icon.noimage");
		_desc = set.getString("desc", "-");
	}
	
	public final int getSkillId()
	{
		return _id;
	}
	
	public final String getIcon()
	{
		return _icon;
	}
	
	public final String getDescription()
	{
		return _desc;
	}
	
	public final int getRequiredItemId()
	{
		return _requiredItemId;
	}
	
	public final int getRequiredItemAmount()
	{
		return _requiredItemAmount;
	}
}