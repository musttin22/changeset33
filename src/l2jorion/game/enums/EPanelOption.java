package l2jorion.game.enums;

/**
 * Enumeración para opciones del panel de Inertia Simplificada para L2JOrion changeset 33 rev 1161
 */
public enum EPanelOption
{
	MAIN_PANEL("Panel Principal"),
	CONFIG_PANEL("Configuración"),
	AUTO_ATTACK("Auto Attack"),
	AUTO_PICKUP("Auto Pickup"),
	AUTO_HEAL("Auto Heal"),
	FOLLOW_MODE("Follow Mode");
	
	private final String _description;
	
	EPanelOption(String description)
	{
		_description = description;
	}
	
	public String getDescription()
	{
		return _description;
	}
	
	@Override
	public String toString()
	{
		return _description;
	}
}